package com.imut.diab_health_sys02.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 糖尿病风险评估工具类（标准风险评分表，与前端 src/utils/diabetesRisk.js 保持一致）
 * 适用范围：20 - 74 岁普通人群；评分范围 0 - 51 分；总分 ≥ 25 分为糖尿病高风险人群。
 *
 * 评分指标：
 *  1) 年龄：20-24→0；25-34→4；35-39→8；40-44→11；45-49→12；50-54→13；55-59→15；60-64→16；65-74→18
 *  2) 体质指数 BMI（kg/m²）：<22.0→0；22.0-23.9→1；24.0-29.9→3；≥30.0→5
 *  3) 腰围（cm）：
 *     男 <75.0 / 女 <70.0 → 0；男 75.0-79.9 / 女 70.0-74.9 → 3；
 *     男 80.0-84.9 / 女 75.0-79.9 → 5；男 85.0-89.9 / 女 80.0-84.9 → 7；
 *     男 90.0-94.9 / 女 85.0-89.9 → 8；男 ≥95.0 / 女 ≥90.0 → 10
 *  4) 收缩压（mmHg）：<110→0；110-119→1；120-129→3；130-139→6；140-149→7；150-159→8；≥160→10
 *  5) 糖尿病家族史（父母、同胞、子女）：无→0；有→6
 *  6) 性别：女→0；男→2
 *
 * 腰围与收缩压为选填项：未填写时根据身高、体重、性别等参数推断。
 *  - 腰围：男 baseWaist = 0.47 × height；女 baseWaist = 0.45 × height；
 *         BMI > 24 时调整：baseWaist × (1 + (BMI - 22) / 10)
 *  - 收缩压：男 BMI<24→115、24≤BMI<28→125、BMI≥28→135；女 BMI<24→110、24≤BMI<28→120、BMI≥28→130
 */
public final class DiabetesRiskUtil {

    private DiabetesRiskUtil() {
    }

    /** 高风险判定阈值 */
    public static final int HIGH_RISK_THRESHOLD = 25;
    /** 中风险判定阈值（< 该值视为低风险） */
    public static final int MID_RISK_THRESHOLD = 15;

    /** 年龄评分表：{ 上限年龄, 分值 } */
    private static final int[][] AGE_TABLE = {
            {24, 0},   // 20 - 24
            {34, 4},   // 25 - 34
            {39, 8},   // 35 - 39
            {44, 11},  // 40 - 44
            {49, 12},  // 45 - 49
            {54, 13},  // 50 - 54
            {59, 15},  // 55 - 59
            {64, 16},  // 60 - 64
            {74, 18}   // 65 - 74
    };

    /** 年龄评分（超出 20-74 范围时按边界档处理；非法值/负数按 0 分） */
    public static int scoreAge(Double age) {
        if (age == null || age <= 0 || age < 20) {
            return 0;
        }
        if (age > 74) {
            return 18;
        }
        for (int[] row : AGE_TABLE) {
            if (age <= row[0]) {
                return row[1];
            }
        }
        return 18;
    }

    /** 体质指数评分（非法值/负数按 0 分） */
    public static int scoreBmi(Double bmi) {
        if (bmi == null || bmi < 22) {
            return 0;
        }
        if (bmi < 24) {
            return 1;
        }
        if (bmi < 30) {
            return 3;
        }
        return 5;
    }

    /** 腰围评分（分性别；非法值/负数按 0 分） */
    public static int scoreWaist(String sex, Double waist) {
        if (waist == null || waist <= 0) {
            return 0;
        }
        boolean male = "男".equals(sex);
        if (male) {
            if (waist < 75) return 0;
            if (waist < 80) return 3;
            if (waist < 85) return 5;
            if (waist < 90) return 7;
            if (waist < 95) return 8;
            return 10;
        }
        if (waist < 70) return 0;
        if (waist < 75) return 3;
        if (waist < 80) return 5;
        if (waist < 85) return 7;
        if (waist < 90) return 8;
        return 10;
    }

    /** 收缩压评分（非法值/负数按 0 分） */
    public static int scoreBp(Double bp) {
        if (bp == null || bp <= 0) {
            return 0;
        }
        if (bp < 110) return 0;
        if (bp < 120) return 1;
        if (bp < 130) return 3;
        if (bp < 140) return 6;
        if (bp < 150) return 7;
        if (bp < 160) return 8;
        return 10;
    }

    /** 糖尿病家族史评分（父母、同胞、子女） */
    public static int scoreFamily(String familyHistory) {
        return "是".equals(familyHistory) ? 6 : 0;
    }

    /** 性别评分 */
    public static int scoreSex(String sex) {
        return "男".equals(sex) ? 2 : 0;
    }

    /** 计算体质指数（身高/体重非法或非正数时返回 null） */
    public static Double calcBmi(Double height, Double weight) {
        if (height == null || weight == null || height <= 0 || weight <= 0) {
            return null;
        }
        double h = height / 100.0;
        return weight / (h * h);
    }

    /**
     * 推断腰围（cm）：
     *  男 baseWaist = 0.47 × height；女 baseWaist = 0.45 × height
     *  BMI > 24 时调整：adjustedWaist = baseWaist × (1 + (BMI - 22) / 10)
     *  （身高非法/非正数或性别缺失时返回 null）
     */
    public static Double predictWaist(String sex, Double height, Double bmi) {
        if (height == null || height <= 0 || sex == null || sex.isEmpty()) {
            return null;
        }
        double base = "男".equals(sex) ? 0.47 * height : 0.45 * height;
        if (bmi == null || bmi <= 0) {
            return (double) Math.round(base);
        }
        double adjusted = bmi > 24 ? base * (1 + (bmi - 22) / 10) : base;
        return (double) Math.round(adjusted);
    }

    /**
     * 推断收缩压（mmHg）：
     *  男：BMI<24→115；24≤BMI<28→125；BMI≥28→135
     *  女：BMI<24→110；24≤BMI<28→120；BMI≥28→130
     *  （BMI 非法/非正数或性别缺失时返回 null）
     */
    public static Double predictBp(String sex, Double bmi) {
        if (bmi == null || bmi <= 0 || sex == null || sex.isEmpty()) {
            return null;
        }
        boolean male = "男".equals(sex);
        if (male) {
            if (bmi < 24) return 115.0;
            if (bmi < 28) return 125.0;
            return 135.0;
        }
        if (bmi < 24) return 110.0;
        if (bmi < 28) return 120.0;
        return 130.0;
    }

    /** 根据评分生成建议文案 */
    public static String buildAdvice(int total) {
        if (total >= HIGH_RISK_THRESHOLD) {
            return "您的糖尿病风险评分为 " + total + " 分（≥" + HIGH_RISK_THRESHOLD
                    + " 分），属于糖尿病高风险人群。建议您尽快前往医院内分泌科进行空腹血糖及口服葡萄糖耐量试验检查，并严格遵循医生建议：控制饮食总热量、坚持规律运动、科学减重，每 3~6 个月复查一次血糖。";
        }
        if (total >= MID_RISK_THRESHOLD) {
            return "您的糖尿病风险评分为 " + total
                    + " 分，风险处于中等水平。建议加强血糖监测，控制精制碳水与高糖食物摄入，坚持每周 150 分钟以上中等强度运动，将体质指数控制在 24 以下，并每年进行一次空腹血糖筛查。";
        }
        return "您的糖尿病风险评分为 " + total
                + " 分，风险较低。请继续保持健康的饮食与运动习惯，控制体重、避免久坐，坚持每年进行一次健康体检。";
    }

    /** 风险等级判定：总分 ≥ 25 → 高风险；15 ≤ 总分 < 25 → 中风险；总分 < 15 → 低风险 */
    public static String levelOf(int total) {
        if (total >= HIGH_RISK_THRESHOLD) {
            return "高风险";
        }
        if (total >= MID_RISK_THRESHOLD) {
            return "中风险";
        }
        return "低风险";
    }

    /** 风险计算参数 */
    public static class RiskParams {
        public Double age;
        public String sex;
        public Double height;
        public Double weight;
        public String familyHistory;
        public Double waistline;
        public Double systolicPressure;
    }

    /** 风险计算结果 */
    public static class RiskResult {
        public int total;
        public String level;
        public List<Map<String, Object>> items = new ArrayList<>();
        public Double bmi;
        public Double waist;
        public Double bp;
        public boolean waistPredicted;
        public boolean bpPredicted;
        public String advice;
    }

    /**
     * 计算糖尿病风险（未患病人群）
     *
     * @param params 年龄/性别/身高/体重/家族史/腰围/收缩压
     * @return 总分、等级、评分明细、BMI、腰围/收缩压（含是否预测）、建议文案
     */
    public static RiskResult calcDiabetesRisk(RiskParams params) {
        RiskResult r = new RiskResult();
        Double age = params.age;
        String sex = ("男".equals(params.sex) || "女".equals(params.sex)) ? params.sex : "";
        Double height = params.height;
        Double weight = params.weight;
        String familyHistory = params.familyHistory;

        r.bmi = calcBmi(height, weight);

        // 腰围 / 收缩压为选填项，未填写或为非法值（负数等）时由其他参数推断
        Double rawWaist = params.waistline;
        Double rawBp = params.systolicPressure;
        Double waist = (rawWaist != null && rawWaist > 0) ? rawWaist : null;
        Double bp = (rawBp != null && rawBp > 0) ? rawBp : null;
        boolean waistPredicted = false;
        boolean bpPredicted = false;
        if (waist == null && !sex.isEmpty() && height != null) {
            waist = predictWaist(sex, height, r.bmi);
            waistPredicted = true;
        }
        if (bp == null && !sex.isEmpty() && r.bmi != null) {
            bp = predictBp(sex, r.bmi);
            bpPredicted = true;
        }

        // 年龄项
        Map<String, Object> ageItem = new LinkedHashMap<>();
        ageItem.put("key", "age");
        ageItem.put("label", "年龄");
        ageItem.put("value", age != null && age > 0 ? numStr(age) + " 岁" : "—");
        ageItem.put("score", scoreAge(age));

        // BMI 项
        Map<String, Object> bmiItem = new LinkedHashMap<>();
        bmiItem.put("key", "bmi");
        bmiItem.put("label", "体质指数 (BMI)");
        bmiItem.put("value", r.bmi != null ? String.format("%.1f", r.bmi) + " kg/m²" : "—");
        bmiItem.put("score", scoreBmi(r.bmi));

        // 腰围项
        Map<String, Object> waistItem = new LinkedHashMap<>();
        waistItem.put("key", "waist");
        waistItem.put("label", "腰围");
        waistItem.put("value", waist != null ? numStr(waist) + " cm" + (waistPredicted ? "（预测）" : "") : "—");
        waistItem.put("score", scoreWaist(sex, waist));
        waistItem.put("predicted", waistPredicted);

        // 收缩压项
        Map<String, Object> bpItem = new LinkedHashMap<>();
        bpItem.put("key", "bp");
        bpItem.put("label", "收缩压");
        bpItem.put("value", bp != null ? numStr(bp) + " mmHg" + (bpPredicted ? "（预测）" : "") : "—");
        bpItem.put("score", scoreBp(bp));
        bpItem.put("predicted", bpPredicted);

        // 家族史项
        Map<String, Object> familyItem = new LinkedHashMap<>();
        familyItem.put("key", "family");
        familyItem.put("label", "糖尿病家族史");
        familyItem.put("value", "是".equals(familyHistory) ? "有" : "否".equals(familyHistory) ? "无" : "—");
        familyItem.put("score", scoreFamily(familyHistory));

        // 性别项
        Map<String, Object> sexItem = new LinkedHashMap<>();
        sexItem.put("key", "sex");
        sexItem.put("label", "性别");
        sexItem.put("value", sex.isEmpty() ? "—" : sex);
        sexItem.put("score", scoreSex(sex));

        r.items.add(ageItem);
        r.items.add(bmiItem);
        r.items.add(waistItem);
        r.items.add(bpItem);
        r.items.add(familyItem);
        r.items.add(sexItem);

        int total = 0;
        for (Map<String, Object> it : r.items) {
            Object s = it.get("score");
            total += (s instanceof Number) ? ((Number) s).intValue() : 0;
        }
        r.total = total;
        r.level = levelOf(total);
        r.waist = waist;
        r.bp = bp;
        r.waistPredicted = waistPredicted;
        r.bpPredicted = bpPredicted;
        r.advice = buildAdvice(total);
        return r;
    }

    /** 数字展示：整数去掉小数位，其余保留原样 */
    private static String numStr(Double v) {
        if (v == null) {
            return "—";
        }
        long l = Math.round(v);
        if (Math.abs(v - l) < 0.000001) {
            return String.valueOf(l);
        }
        return String.valueOf(v);
    }
}
