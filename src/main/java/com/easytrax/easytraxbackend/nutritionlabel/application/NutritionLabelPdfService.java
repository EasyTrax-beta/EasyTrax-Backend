package com.easytrax.easytraxbackend.nutritionlabel.application;

import com.easytrax.easytraxbackend.global.pdf.PdfService;
import com.easytrax.easytraxbackend.nutritionlabel.domain.LabelFormat;
import com.easytrax.easytraxbackend.nutritionlabel.domain.NutritionLabel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class NutritionLabelPdfService {

    private final PdfService pdfService;

    public byte[] generateNutritionLabelPdf(NutritionLabel nutritionLabel) {
        String htmlContent = generateHtmlContent(nutritionLabel);
        return pdfService.generatePdfFromHtml(htmlContent);
    }

    private String generateHtmlContent(NutritionLabel nutritionLabel) {
        return switch (nutritionLabel.getLabelFormat()) {
            case USA_FDA -> generateUsaFdaFormatHtml(nutritionLabel);
            case CHINA_GB28050 -> generateChinaGb28050FormatHtml(nutritionLabel);
            case JAPAN_JAS -> generateJapanJasFormatHtml(nutritionLabel);
            case EU_1169 -> generateEu1169FormatHtml(nutritionLabel);
        };
    }

    private String generateUsaFdaFormatHtml(NutritionLabel nutritionLabel) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        .nutrition-facts { 
                            border: 3px solid black; 
                            width: 300px; 
                            padding: 10px; 
                            font-size: 12px; 
                        }
                        .title { font-size: 24px; font-weight: bold; text-align: center; margin-bottom: 5px; }
                        .serving-info { font-size: 14px; margin-bottom: 10px; }
                        .serving-size { font-weight: bold; }
                        .calories { 
                            font-size: 18px; 
                            font-weight: bold; 
                            border-top: 8px solid black; 
                            border-bottom: 4px solid black; 
                            padding: 5px 0; 
                        }
                        .daily-value { text-align: right; font-size: 10px; margin-bottom: 5px; }
                        .nutrient-row { 
                            display: flex; 
                            justify-content: space-between; 
                            padding: 2px 0; 
                            border-bottom: 1px solid #ccc; 
                        }
                        .indent { margin-left: 20px; }
                        .bold { font-weight: bold; }
                        .thick-border { border-bottom: 4px solid black; }
                        .footer { font-size: 10px; margin-top: 10px; }
                    </style>
                </head>
                <body>
                    <div class="nutrition-facts">
                        <div class="title">Nutrition Facts</div>
                        <div class="serving-info">
                            <div>%d servings per container</div>
                            <div class="serving-size">Serving size %s</div>
                        </div>
                        
                        <div class="calories">
                            <div style="display: flex; justify-content: space-between;">
                                <span>Calories</span>
                                <span style="font-size: 28px;">%d</span>
                            </div>
                        </div>
                        
                        <div class="daily-value">%% Daily Value*</div>
                        
                        <div class="nutrient-row">
                            <span class="bold">Total Fat %sg</span>
                            <span class="bold">%d%%</span>
                        </div>
                        
                        <div class="nutrient-row indent">
                            <span>Saturated Fat %sg</span>
                            <span class="bold">%d%%</span>
                        </div>
                        
                        <div class="nutrient-row indent">
                            <span><i>Trans</i> Fat %sg</span>
                            <span></span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span class="bold">Cholesterol %smg</span>
                            <span class="bold">%d%%</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span class="bold">Sodium %smg</span>
                            <span class="bold">%d%%</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span class="bold">Total Carbohydrate %sg</span>
                            <span class="bold">%d%%</span>
                        </div>
                        
                        <div class="nutrient-row indent">
                            <span>Dietary Fiber %sg</span>
                            <span class="bold">%d%%</span>
                        </div>
                        
                        <div class="nutrient-row indent">
                            <span>Total Sugars %sg</span>
                            <span></span>
                        </div>
                        
                        <div class="nutrient-row indent" style="margin-left: 40px;">
                            <span>Includes %sg Added Sugars</span>
                            <span class="bold">%d%%</span>
                        </div>
                        
                        <div class="nutrient-row thick-border">
                            <span class="bold">Protein %sg</span>
                            <span></span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>Vitamin D %smcg</span>
                            <span>%d%%</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>Calcium %smg</span>
                            <span>%d%%</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>Iron %smg</span>
                            <span>%d%%</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>Potassium %smg</span>
                            <span>%d%%</span>
                        </div>
                        
                        <div class="footer">
                            * The %% Daily Value (DV) tells you how much a nutrient in a serving of food contributes to a daily diet. 2,000 calories a day is used for general nutrition advice.
                        </div>
                    </div>
                </body>
                </html>
                """,
                nutritionLabel.getServingsPerContainer(),
                nutritionLabel.getServingSize(),
                nutritionLabel.getCalories(),
                formatValue(nutritionLabel.getTotalFat()),
                calculateDailyValue(nutritionLabel.getTotalFat(), new BigDecimal("65")),
                formatValue(nutritionLabel.getSaturatedFat()),
                calculateDailyValue(nutritionLabel.getSaturatedFat(), new BigDecimal("20")),
                formatValue(nutritionLabel.getTransFat()),
                formatValue(nutritionLabel.getCholesterol()),
                calculateDailyValue(nutritionLabel.getCholesterol(), new BigDecimal("300")),
                formatValue(nutritionLabel.getSodium()),
                calculateDailyValue(nutritionLabel.getSodium(), new BigDecimal("2300")),
                formatValue(nutritionLabel.getTotalCarbohydrate()),
                calculateDailyValue(nutritionLabel.getTotalCarbohydrate(), new BigDecimal("300")),
                formatValue(nutritionLabel.getDietaryFiber()),
                calculateDailyValue(nutritionLabel.getDietaryFiber(), new BigDecimal("28")),
                formatValue(nutritionLabel.getTotalSugars()),
                formatValue(nutritionLabel.getAddedSugars()),
                calculateDailyValue(nutritionLabel.getAddedSugars(), new BigDecimal("50")),
                formatValue(nutritionLabel.getProtein()),
                formatValue(nutritionLabel.getVitaminD()),
                calculateDailyValue(nutritionLabel.getVitaminD(), new BigDecimal("20")),
                formatValue(nutritionLabel.getCalcium()),
                calculateDailyValue(nutritionLabel.getCalcium(), new BigDecimal("1300")),
                formatValue(nutritionLabel.getIron()),
                calculateDailyValue(nutritionLabel.getIron(), new BigDecimal("18")),
                formatValue(nutritionLabel.getPotassium()),
                calculateDailyValue(nutritionLabel.getPotassium(), new BigDecimal("4700"))
        );
    }

    private String generateChinaGb28050FormatHtml(NutritionLabel nutritionLabel) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: "Microsoft YaHei", "SimHei", Arial, sans-serif; margin: 20px; }
                        .nutrition-facts { 
                            border: 2px solid black; 
                            width: 280px; 
                            padding: 10px; 
                            font-size: 12px; 
                        }
                        .title { font-size: 18px; font-weight: bold; text-align: center; margin-bottom: 10px; }
                        .serving-info { font-size: 11px; margin-bottom: 8px; text-align: center; }
                        .header-row { 
                            display: flex; 
                            justify-content: space-between; 
                            font-weight: bold; 
                            border-top: 2px solid black; 
                            border-bottom: 1px solid black; 
                            padding: 3px 0; 
                            font-size: 10px;
                        }
                        .nutrient-row { 
                            display: flex; 
                            justify-content: space-between; 
                            padding: 2px 0; 
                            border-bottom: 1px solid #ccc; 
                            font-size: 11px;
                        }
                        .bold { font-weight: bold; }
                        .footer { font-size: 9px; margin-top: 8px; border-top: 1px solid black; padding-top: 3px; }
                    </style>
                </head>
                <body>
                    <div class="nutrition-facts">
                        <div class="title">营养成分表</div>
                        <div class="serving-info">每份: %s &nbsp;&nbsp; 每包装份数: %d</div>
                        
                        <div class="header-row">
                            <span>项目</span>
                            <span>每份</span>
                            <span>NRV%%*</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>能量</span>
                            <span>%d千焦</span>
                            <span>%d%%</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>蛋白质</span>
                            <span>%sg</span>
                            <span>%d%%</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>脂肪</span>
                            <span>%sg</span>
                            <span>%d%%</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>　－饱和脂肪</span>
                            <span>%sg</span>
                            <span>%d%%</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>　－反式脂肪</span>
                            <span>%sg</span>
                            <span>-</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>碳水化合物</span>
                            <span>%sg</span>
                            <span>%d%%</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>　－糖</span>
                            <span>%sg</span>
                            <span>-</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>　－膳食纤维</span>
                            <span>%sg</span>
                            <span>%d%%</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>钠</span>
                            <span>%smg</span>
                            <span>%d%%</span>
                        </div>
                        
                        <div class="nutrient-row">
                            <span>胆固醇</span>
                            <span>%smg</span>
                            <span>-</span>
                        </div>
                        
                        <div class="footer">
                            * 营养素参考值%%NRV
                        </div>
                    </div>
                </body>
                </html>
                """,
                nutritionLabel.getServingSize(),
                nutritionLabel.getServingsPerContainer(),
                convertCaloriesToKilojoules(nutritionLabel.getCalories()),
                calculateChinaDailyValue(convertCaloriesToKilojoules(nutritionLabel.getCalories()), 8400),
                formatValue(nutritionLabel.getProtein()),
                calculateChinaDailyValue(nutritionLabel.getProtein(), new BigDecimal("60")),
                formatValue(nutritionLabel.getTotalFat()),
                calculateChinaDailyValue(nutritionLabel.getTotalFat(), new BigDecimal("60")),
                formatValue(nutritionLabel.getSaturatedFat()),
                calculateChinaDailyValue(nutritionLabel.getSaturatedFat(), new BigDecimal("20")),
                formatValue(nutritionLabel.getTransFat()),
                formatValue(nutritionLabel.getTotalCarbohydrate()),
                calculateChinaDailyValue(nutritionLabel.getTotalCarbohydrate(), new BigDecimal("300")),
                formatValue(nutritionLabel.getTotalSugars()),
                formatValue(nutritionLabel.getDietaryFiber()),
                calculateChinaDailyValue(nutritionLabel.getDietaryFiber(), new BigDecimal("25")),
                formatValue(nutritionLabel.getSodium()),
                calculateChinaDailyValue(nutritionLabel.getSodium(), new BigDecimal("2000")),
                formatValue(nutritionLabel.getCholesterol())
        );
    }

    private String generateJapanJasFormatHtml(NutritionLabel nutritionLabel) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: "Hiragino Sans", "Yu Gothic", "Meiryo", Arial, sans-serif; margin: 20px; }
                        .nutrition-facts { 
                            border: 2px solid black; 
                            width: 300px; 
                            padding: 10px; 
                            font-size: 12px; 
                        }
                        .title { font-size: 16px; font-weight: bold; text-align: center; margin-bottom: 10px; }
                        .serving-info { font-size: 11px; margin-bottom: 8px; text-align: center; }
                        .nutrient-table { 
                            width: 100%%; 
                            border-collapse: collapse; 
                            margin-top: 10px; 
                        }
                        .nutrient-table th, .nutrient-table td { 
                            border: 1px solid black; 
                            padding: 4px; 
                            text-align: left; 
                            font-size: 11px; 
                        }
                        .nutrient-table th { 
                            background-color: #f0f0f0; 
                            font-weight: bold; 
                            text-align: center; 
                        }
                        .nutrient-row { 
                            display: flex; 
                            justify-content: space-between; 
                            padding: 2px 0; 
                            border-bottom: 1px solid #ccc; 
                        }
                        .footer { font-size: 9px; margin-top: 8px; }
                    </style>
                </head>
                <body>
                    <div class="nutrition-facts">
                        <div class="title">栄養成分表示</div>
                        <div class="serving-info">1食分（%s）当たり</div>
                        
                        <table class="nutrient-table">
                            <tr>
                                <td style="font-weight: bold;">エネルギー</td>
                                <td>%d kcal</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold;">たんぱく質</td>
                                <td>%s g</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold;">脂質</td>
                                <td>%s g</td>
                            </tr>
                            <tr>
                                <td style="padding-left: 15px;">－飽和脂肪酸</td>
                                <td>%s g</td>
                            </tr>
                            <tr>
                                <td style="padding-left: 15px;">－トランス脂肪酸</td>
                                <td>%s g</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold;">炭水化物</td>
                                <td>%s g</td>
                            </tr>
                            <tr>
                                <td style="padding-left: 15px;">－糖質</td>
                                <td>%s g</td>
                            </tr>
                            <tr>
                                <td style="padding-left: 15px;">－食物繊維</td>
                                <td>%s g</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold;">食塩相当量</td>
                                <td>%s g</td>
                            </tr>
                            <tr>
                                <td>コレステロール</td>
                                <td>%s mg</td>
                            </tr>
                            <tr>
                                <td>カルシウム</td>
                                <td>%s mg</td>
                            </tr>
                            <tr>
                                <td>鉄</td>
                                <td>%s mg</td>
                            </tr>
                            <tr>
                                <td>ビタミンC</td>
                                <td>%s mg</td>
                            </tr>
                        </table>
                        
                        <div class="footer">
                            ※この表示値は目安です。
                        </div>
                    </div>
                </body>
                </html>
                """,
                nutritionLabel.getServingSize(),
                nutritionLabel.getCalories(),
                formatValue(nutritionLabel.getProtein()),
                formatValue(nutritionLabel.getTotalFat()),
                formatValue(nutritionLabel.getSaturatedFat()),
                formatValue(nutritionLabel.getTransFat()),
                formatValue(nutritionLabel.getTotalCarbohydrate()),
                formatValueSubtract(nutritionLabel.getTotalCarbohydrate(), nutritionLabel.getDietaryFiber()), // 糖質 = 炭水化物 - 食物繊維
                formatValue(nutritionLabel.getDietaryFiber()),
                formatSodiumToSalt(nutritionLabel.getSodium()), // 食塩相当量 = ナトリウム × 2.54 ÷ 1000
                formatValue(nutritionLabel.getCholesterol()),
                formatValue(nutritionLabel.getCalcium()),
                formatValue(nutritionLabel.getIron()),
                formatValue(nutritionLabel.getVitaminC())
        );
    }

    private String generateEu1169FormatHtml(NutritionLabel nutritionLabel) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        .nutrition-facts { 
                            border: 1px solid black; 
                            width: 350px; 
                            padding: 10px; 
                            font-size: 12px; 
                        }
                        .title { font-size: 16px; font-weight: bold; margin-bottom: 10px; }
                        .serving-info { font-size: 11px; margin-bottom: 8px; }
                        .nutrient-table { 
                            width: 100%%; 
                            border-collapse: collapse; 
                            margin-top: 10px; 
                        }
                        .nutrient-table th, .nutrient-table td { 
                            border: none; 
                            border-bottom: 1px solid #ccc;
                            padding: 3px; 
                            text-align: left; 
                            font-size: 11px; 
                        }
                        .nutrient-table th { 
                            font-weight: bold; 
                        }
                        .nutrient-table .per100g { 
                            text-align: right; 
                            font-weight: bold; 
                        }
                        .nutrient-table .ri { 
                            text-align: right; 
                        }
                        .bold-row { font-weight: bold; }
                        .indent { padding-left: 15px; }
                        .footer { font-size: 9px; margin-top: 8px; }
                    </style>
                </head>
                <body>
                    <div class="nutrition-facts">
                        <div class="title">Nutrition Information</div>
                        <div class="serving-info">Typical values per 100g:</div>
                        
                        <table class="nutrient-table">
                            <thead>
                                <tr>
                                    <th style="width: 60%%;">Nutrition</th>
                                    <th style="width: 25%%; text-align: right;">Per 100g</th>
                                    <th style="width: 15%%; text-align: right;">RI*</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr class="bold-row">
                                    <td>Energy</td>
                                    <td class="per100g">%d kJ / %d kcal</td>
                                    <td class="ri">%d%%</td>
                                </tr>
                                <tr class="bold-row">
                                    <td>Fat</td>
                                    <td class="per100g">%s g</td>
                                    <td class="ri">%d%%</td>
                                </tr>
                                <tr>
                                    <td class="indent">of which saturates</td>
                                    <td class="per100g">%s g</td>
                                    <td class="ri">%d%%</td>
                                </tr>
                                <tr class="bold-row">
                                    <td>Carbohydrate</td>
                                    <td class="per100g">%s g</td>
                                    <td class="ri">%d%%</td>
                                </tr>
                                <tr>
                                    <td class="indent">of which sugars</td>
                                    <td class="per100g">%s g</td>
                                    <td class="ri">%d%%</td>
                                </tr>
                                <tr class="bold-row">
                                    <td>Fibre</td>
                                    <td class="per100g">%s g</td>
                                    <td class="ri">-</td>
                                </tr>
                                <tr class="bold-row">
                                    <td>Protein</td>
                                    <td class="per100g">%s g</td>
                                    <td class="ri">%d%%</td>
                                </tr>
                                <tr class="bold-row">
                                    <td>Salt</td>
                                    <td class="per100g">%s g</td>
                                    <td class="ri">%d%%</td>
                                </tr>
                            </tbody>
                        </table>
                        
                        <div class="footer">
                            * Reference intake of an average adult (8400 kJ / 2000 kcal)
                        </div>
                    </div>
                </body>
                </html>
                """,
                convertCaloriesToKilojoules(nutritionLabel.getCalories()),
                nutritionLabel.getCalories(),
                calculateEuDailyValue(nutritionLabel.getCalories(), 2000),
                formatValue(nutritionLabel.getTotalFat()),
                calculateEuDailyValue(nutritionLabel.getTotalFat(), new BigDecimal("70")),
                formatValue(nutritionLabel.getSaturatedFat()),
                calculateEuDailyValue(nutritionLabel.getSaturatedFat(), new BigDecimal("20")),
                formatValue(nutritionLabel.getTotalCarbohydrate()),
                calculateEuDailyValue(nutritionLabel.getTotalCarbohydrate(), new BigDecimal("260")),
                formatValue(nutritionLabel.getTotalSugars()),
                calculateEuDailyValue(nutritionLabel.getTotalSugars(), new BigDecimal("90")),
                formatValue(nutritionLabel.getDietaryFiber()),
                formatValue(nutritionLabel.getProtein()),
                calculateEuDailyValue(nutritionLabel.getProtein(), new BigDecimal("50")),
                formatSodiumToSalt(nutritionLabel.getSodium()),
                calculateEuDailyValue(convertSodiumToSaltValue(nutritionLabel.getSodium()), new BigDecimal("6"))
        );
    }


    private String formatValue(BigDecimal value) {
        if (value == null) return "0";
        return value.stripTrailingZeros().toPlainString();
    }

    private int calculateDailyValue(BigDecimal nutrientValue, BigDecimal dailyValue) {
        if (nutrientValue == null || dailyValue == null || dailyValue.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return nutrientValue.multiply(new BigDecimal("100"))
                .divide(dailyValue, 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
    }

    private int calculateChinaDailyValue(BigDecimal nutrientValue, BigDecimal dailyValue) {
        if (nutrientValue == null || dailyValue == null || dailyValue.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return nutrientValue.multiply(new BigDecimal("100"))
                .divide(dailyValue, 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
    }

    private int calculateChinaDailyValue(int kilojoules, int dailyKilojoules) {
        if (dailyKilojoules == 0) return 0;
        return Math.round((float) kilojoules * 100 / dailyKilojoules);
    }

    private int convertCaloriesToKilojoules(Integer calories) {
        if (calories == null) return 0;
        return Math.round(calories * 4.184f);
    }

    private int calculateEuDailyValue(BigDecimal nutrientValue, BigDecimal dailyValue) {
        if (nutrientValue == null || dailyValue == null || dailyValue.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return nutrientValue.multiply(new BigDecimal("100"))
                .divide(dailyValue, 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
    }

    private int calculateEuDailyValue(Integer calories, int dailyCalories) {
        if (calories == null || dailyCalories == 0) return 0;
        return Math.round((float) calories * 100 / dailyCalories);
    }

    private String formatValueSubtract(BigDecimal value1, BigDecimal value2) {
        if (value1 == null) value1 = BigDecimal.ZERO;
        if (value2 == null) value2 = BigDecimal.ZERO;
        BigDecimal result = value1.subtract(value2);
        if (result.compareTo(BigDecimal.ZERO) < 0) result = BigDecimal.ZERO;
        return result.stripTrailingZeros().toPlainString();
    }

    private String formatSodiumToSalt(BigDecimal sodium) {
        if (sodium == null) return "0";
        BigDecimal salt = sodium.multiply(new BigDecimal("2.54")).divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP);
        return salt.stripTrailingZeros().toPlainString();
    }

    private BigDecimal convertSodiumToSaltValue(BigDecimal sodium) {
        if (sodium == null) return BigDecimal.ZERO;
        return sodium.multiply(new BigDecimal("2.54")).divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP);
    }
}