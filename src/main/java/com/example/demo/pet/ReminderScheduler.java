package com.example.demo.pet;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReminderScheduler {
    private final ReminderRepository reminderRepository;
    private final VaccinationRepository vaccinationRepository;
    private final PetRepository petRepository;
    private final VaccineRepository vaccineRepository;
    private final WeChatService weChatService;

    public ReminderScheduler(ReminderRepository reminderRepository, 
                          VaccinationRepository vaccinationRepository,
                          PetRepository petRepository,
                          VaccineRepository vaccineRepository,
                          WeChatService weChatService) {
        this.reminderRepository = reminderRepository;
        this.vaccinationRepository = vaccinationRepository;
        this.petRepository = petRepository;
        this.vaccineRepository = vaccineRepository;
        this.weChatService = weChatService;
    }

    // 每天凌晨1点执行一次
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDueReminders() {
        // 获取今天的日期
        LocalDate today = LocalDate.now();
        System.out.println("=== 开始处理今天的提醒 ===");
        System.out.println("处理日期: " + today);
        
        // 查询今天需要提醒的记录
        List<Reminder> dueReminders = reminderRepository.listByDate(today);
        System.out.println("找到 " + dueReminders.size() + " 条需要提醒的记录");
        
        // 处理每个提醒
        for (Reminder reminder : dueReminders) {
            System.out.println("\n处理提醒 ID: " + reminder.getId());
            System.out.println("提醒事项: " + reminder.getReminderThing());
            System.out.println("提醒日期: " + reminder.getReminderDate());
            System.out.println("目标名称: " + reminder.getTargetName());
            System.out.println("OpenID: " + reminder.getOpenid());
            
            // 只处理未发送的提醒
            if (!Boolean.TRUE.equals(reminder.getSent())) {
                System.out.println("状态: 未发送，开始处理...");
                try {
                    // 发送微信订阅消息
                    boolean sent = sendWechatMessage(reminder);
                    
                    // 更新提醒状态
                    if (sent) {
                        reminder.setSent(true);
                        reminder.setSentAt(java.time.Instant.now());
                        reminder.setSendError(null);
                        System.out.println("状态: 发送成功");
                    }
                } catch (Exception e) {
                    // 记录发送失败的错误信息
                    reminder.setSent(false);
                    reminder.setSendError(e.getMessage());
                    System.out.println("状态: 发送失败");
                    System.out.println("错误信息: " + e.getMessage());
                } finally {
                    // 更新提醒记录
                    reminderRepository.update(reminder);
                    System.out.println("状态: 已更新提醒记录");
                }
            } else {
                System.out.println("状态: 已发送，跳过处理");
            }
        }
        System.out.println("\n=== 今天的提醒处理完成 ===");
    }

    private boolean sendWechatMessage(Reminder reminder) throws Exception {
        System.out.println("\n=== 开始发送微信消息 ===");
        
        // 获取相关信息
        System.out.println("1. 获取疫苗接种记录...");
        Vaccination vaccination = vaccinationRepository.findById(reminder.getVaccinationId());
        if (vaccination == null) {
            throw new Exception("疫苗接种记录不存在");
        }
        System.out.println("   疫苗接种记录 ID: " + vaccination.getId());
        
        // 获取宠物信息
        System.out.println("2. 获取宠物信息...");
        Pet pet = petRepository.findById(vaccination.getPetId())
                .orElseThrow(() -> new Exception("宠物不存在"));
        System.out.println("   宠物名称: " + pet.getName());
        System.out.println("   宠物品种: " + pet.getBreed());
        
        // 获取疫苗信息
        System.out.println("3. 获取疫苗信息...");
        List<Vaccine> vaccines = vaccineRepository.list(null);
        Vaccine vaccine = vaccines.stream()
                .filter(v -> v.getId().equals(vaccination.getVaccineId()))
                .findFirst()
                .orElseThrow(() -> new Exception("疫苗不存在"));
        System.out.println("   疫苗名称: " + vaccine.getName());
        
        // 构建消息数据
        System.out.println("4. 构建消息数据...");
        Map<String, Map<String, String>> data = new HashMap<>();
        
        // 提醒日期
        Map<String, String> date2 = new HashMap<>();
        String dateValue = reminder.getReminderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        date2.put("value", dateValue);
        data.put("date2", date2);
        System.out.println("   提醒日期: " + dateValue);
        
        // 提醒事项
        Map<String, String> thing3 = new HashMap<>();
        thing3.put("value", reminder.getReminderThing());
        data.put("thing3", thing3);
        System.out.println("   提醒事项: " + reminder.getReminderThing());
        
        // 事项地点
        Map<String, String> thing4 = new HashMap<>();
        String locationValue = reminder.getLocation() != null ? reminder.getLocation() : "无";
        thing4.put("value", locationValue);
        data.put("thing4", thing4);
        System.out.println("   事项地点: " + locationValue);
        
        // 目标名称
        Map<String, String> thing7 = new HashMap<>();
        thing7.put("value", reminder.getTargetName());
        data.put("thing7", thing7);
        System.out.println("   目标名称: " + reminder.getTargetName());
        
        // 备注
        Map<String, String> thing9 = new HashMap<>();
        String notesValue = reminder.getNotes() != null ? reminder.getNotes() : "无";
        thing9.put("value", notesValue);
        data.put("thing9", thing9);
        System.out.println("   备注: " + notesValue);
        
        // 获取用户的openid
        System.out.println("5. 获取用户OpenID...");
        String openid = reminder.getOpenid();
        if (openid == null || openid.isEmpty()) {
            throw new Exception("OpenID为空，无法发送消息");
        }
        System.out.println("   OpenID: " + openid);
        
        // 发送订阅消息
        System.out.println("6. 发送订阅消息...");
        System.out.println("   模板ID: " + reminder.getTemplateId());
        Map<String, Object> result = weChatService.sendSubscribeMessage(
                reminder.getTemplateId(),
                openid,
                data
        );
        
        // 检查发送结果
        System.out.println("7. 检查发送结果...");
        System.out.println("   发送结果: " + result);
        if (result.containsKey("errcode") && ((Number) result.get("errcode")).intValue() == 0) {
            System.out.println("   发送成功！");
            System.out.println("=== 微信消息发送完成 ===");
            return true;
        } else {
            throw new Exception("发送消息失败: " + result.get("errmsg"));
        }
    }
}