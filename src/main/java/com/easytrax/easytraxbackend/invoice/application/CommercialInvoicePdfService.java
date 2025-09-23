package com.easytrax.easytraxbackend.invoice.application;

import com.easytrax.easytraxbackend.global.pdf.PdfService;
import com.easytrax.easytraxbackend.invoice.domain.CommercialInvoice;
import com.easytrax.easytraxbackend.invoice.domain.CommercialInvoiceItem;
import com.easytrax.easytraxbackend.invoice.domain.InvoiceFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CommercialInvoicePdfService {

    private final PdfService pdfService;
    
    private String esc(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }

    public byte[] generateCommercialInvoicePdf(CommercialInvoice invoice) {
        String htmlContent = switch (invoice.getInvoiceFormat()) {
            case USA_STANDARD -> generateUsaStandardHtml(invoice);
            case CHINA_STANDARD -> generateChinaStandardHtml(invoice);
            case JAPAN_STANDARD -> generateJapanStandardHtml(invoice);
            case EU_STANDARD -> generateEuStandardHtml(invoice);
        };
        return pdfService.generatePdfFromHtml(htmlContent);
    }

    private String generateUsaStandardHtml(CommercialInvoice invoice) {
        StringBuilder itemsHtml = new StringBuilder();
        
        for (CommercialInvoiceItem item : invoice.getItems()) {
            String hsCode = item.getHsCode() != null ? esc(item.getHsCode()) : "N/A";
            String countryOfOrigin = item.getCountryOfOrigin() != null ? esc(item.getCountryOfOrigin()) : "N/A";
            
            itemsHtml.append(String.format("""
                    <tr>
                        <td style="border: 1px solid black; padding: 5px; text-align: center;">%d</td>
                        <td style="border: 1px solid black; padding: 5px; text-align: center;">%s</td>
                        <td style="border: 1px solid black; padding: 5px;">%s<br><small>HS Code: %s<br>Origin: %s</small></td>
                        <td style="border: 1px solid black; padding: 5px; text-align: center;">%d</td>
                        <td style="border: 1px solid black; padding: 5px; text-align: right;">$%s</td>
                        <td style="border: 1px solid black; padding: 5px; text-align: right;">$%s</td>
                    </tr>
                    """,
                    item.getPackageCount(),
                    esc(item.getPackageType()),
                    esc(item.getGoodsDescription()),
                    hsCode,
                    countryOfOrigin,
                    item.getQuantity(),
                    item.getUnitPrice().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                    item.getAmount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
            ));
        }

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; font-size: 12px; }
                        .header { text-align: center; font-size: 18px; font-weight: bold; margin-bottom: 20px; }
                        .info-section { margin-bottom: 15px; }
                        .info-row { display: flex; margin-bottom: 10px; }
                        .info-left, .info-right { flex: 1; padding: 0 10px; }
                        .info-box { border: 1px solid black; padding: 8px; margin-bottom: 5px; }
                        .label { font-weight: bold; }
                        table { width: 100%%; border-collapse: collapse; margin: 15px 0; }
                        th, td { border: 1px solid black; padding: 5px; text-align: left; }
                        th { background-color: #f0f0f0; font-weight: bold; text-align: center; }
                        .signature-section { margin-top: 30px; text-align: right; }
                        .signature-box { border: 1px solid black; padding: 10px; width: 200px; margin-left: auto; }
                    </style>
                </head>
                <body>
                    <div class="header">COMMERCIAL INVOICE</div>
                    
                    <div class="info-section">
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">① Shipper/Seller</div>
                                    <div>%s</div>
                                    <div>%s</div>
                                    %s
                                </div>
                            </div>
                            <div class="info-right">
                                <div class="info-box">
                                    <div class="label">⑦ Invoice No. and date</div>
                                    <div>Invoice No: %s</div>
                                    <div>Date: %s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">② Consignee (or For account & risk of Messrs)</div>
                                    <div>%s</div>
                                    %s
                                    <div>L/C No: %s</div>
                                    <div>Date: %s</div>
                                </div>
                            </div>
                            <div class="info-right">
                                <div class="info-box">
                                    <div class="label">⑧ L/C No. and date</div>
                                    <div>%s</div>
                                </div>
                                <div class="info-box">
                                    <div class="label">⑨ Buyer(if other than consignee)</div>
                                    <div>%s</div>
                                    <div>%s</div>
                                    %s
                                </div>
                                <div class="info-box">
                                    <div class="label">⑩ Other references</div>
                                    <div>%s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">③ Departure date</div>
                                    <div>%s</div>
                                </div>
                            </div>
                            <div class="info-right">
                                <div class="info-box">
                                    <div class="label">⑪ Terms of delivery and payment</div>
                                    <div>Terms: %s</div>
                                    <div>Payment: %s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">④ Vessel/flight</div>
                                    <div>%s</div>
                                    <div class="label">⑤From</div>
                                    <div>%s</div>
                                </div>
                            </div>
                            <div class="info-right"></div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">⑥ To</div>
                                    <div>%s</div>
                                </div>
                            </div>
                            <div class="info-right"></div>
                        </div>
                    </div>
                    
                    <table>
                        <thead>
                            <tr>
                                <th>⑫Shipping Marks<br>%s</th>
                                <th>⑬No.&kind of packages</th>
                                <th>⑭Goods description</th>
                                <th>⑮Quantity</th>
                                <th>⑯Unit price</th>
                                <th>⑰Amount</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>
                    
                    <div style="text-align: right; margin-top: 20px;">
                        <div style="font-size: 16px; font-weight: bold;">
                            Total Amount: $%s
                        </div>
                    </div>
                    
                    <div class="signature-section">
                        <div class="signature-box">
                            <div class="label">Signed by</div>
                            <div style="margin-top: 20px;">⑱ %s</div>
                        </div>
                    </div>
                </body>
                </html>
                """,
                esc(invoice.getShipperSellerName()),
                esc(invoice.getShipperSellerAddress()),
                invoice.getShipperSellerPhone() != null ? "<div>Tel: " + esc(invoice.getShipperSellerPhone()) + "</div>" : "",
                esc(invoice.getInvoiceNumber()),
                invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                invoice.getConsigneeName() != null ? esc(invoice.getConsigneeName()) : "",
                invoice.getConsigneeAddress() != null ? "<div>" + esc(invoice.getConsigneeAddress()) + "</div>" : "",
                invoice.getLcNumber() != null ? esc(invoice.getLcNumber()) : "N/A",
                invoice.getLcDate() != null ? invoice.getLcDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
                invoice.getLcNumber() != null ? esc(invoice.getLcNumber()) : "N/A",
                esc(invoice.getBuyerName()),
                esc(invoice.getBuyerAddress()),
                invoice.getBuyerPhone() != null ? "<div>Tel: " + esc(invoice.getBuyerPhone()) + "</div>" : "",
                invoice.getOtherReferences() != null ? esc(invoice.getOtherReferences()) : "N/A",
                invoice.getDepartureDate() != null ? invoice.getDepartureDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
                invoice.getTermsOfDelivery() != null ? esc(invoice.getTermsOfDelivery()) : "",
                invoice.getPaymentTerms() != null ? esc(invoice.getPaymentTerms()) : "",
                invoice.getVesselFlight() != null ? esc(invoice.getVesselFlight()) : "N/A",
                esc(invoice.getFromCountry()),
                invoice.getToDestination() != null ? esc(invoice.getToDestination()) : "N/A",
                invoice.getShippingMarks() != null ? esc(invoice.getShippingMarks()) : "N/A",
                itemsHtml.toString(),
                invoice.getTotalAmount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                esc(invoice.getShipperSellerName())
        );
    }

    private String generateChinaStandardHtml(CommercialInvoice invoice) {
        StringBuilder itemsHtml = new StringBuilder();
        
        for (CommercialInvoiceItem item : invoice.getItems()) {
            String hsCode = item.getHsCode() != null ? esc(item.getHsCode()) : "N/A";
            String countryOfOrigin = item.getCountryOfOrigin() != null ? esc(item.getCountryOfOrigin()) : "N/A";
            
            itemsHtml.append(String.format("""
                    <tr>
                        <td style="border: 1px solid black; padding: 5px; text-align: center;">%d</td>
                        <td style="border: 1px solid black; padding: 5px; text-align: center;">%s</td>
                        <td style="border: 1px solid black; padding: 5px;">%s<br><small>HS编码: %s<br>原产地: %s</small></td>
                        <td style="border: 1px solid black; padding: 5px; text-align: center;">%d</td>
                        <td style="border: 1px solid black; padding: 5px; text-align: right;">¥%s</td>
                        <td style="border: 1px solid black; padding: 5px; text-align: right;">¥%s</td>
                    </tr>
                    """,
                    item.getPackageCount(),
                    esc(item.getPackageType()),
                    esc(item.getGoodsDescription()),
                    hsCode,
                    countryOfOrigin,
                    item.getQuantity(),
                    item.getUnitPrice().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                    item.getAmount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
            ));
        }

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: "SimSun", Arial, sans-serif; margin: 20px; font-size: 12px; }
                        .header { text-align: center; font-size: 18px; font-weight: bold; margin-bottom: 20px; }
                        .header-cn { text-align: center; font-size: 16px; margin-bottom: 10px; }
                        .info-section { margin-bottom: 15px; }
                        .info-row { display: flex; margin-bottom: 10px; }
                        .info-left, .info-right { flex: 1; padding: 0 10px; }
                        .info-box { border: 1px solid black; padding: 8px; margin-bottom: 5px; }
                        .label { font-weight: bold; }
                        table { width: 100%%; border-collapse: collapse; margin: 15px 0; }
                        th, td { border: 1px solid black; padding: 5px; text-align: left; }
                        th { background-color: #f0f0f0; font-weight: bold; text-align: center; }
                        .signature-section { margin-top: 30px; text-align: right; }
                        .signature-box { border: 1px solid black; padding: 10px; width: 200px; margin-left: auto; }
                    </style>
                </head>
                <body>
                    <div class="header">商业发票 COMMERCIAL INVOICE</div>
                    <div class="header-cn">出口商品发票</div>
                    
                    <div class="info-section">
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">① 发货人/卖方 Shipper/Seller</div>
                                    <div>%s</div>
                                    <div>%s</div>
                                    %s
                                </div>
                            </div>
                            <div class="info-right">
                                <div class="info-box">
                                    <div class="label">⑦ 发票号码及日期 Invoice No. and date</div>
                                    <div>发票号码 Invoice No: %s</div>
                                    <div>日期 Date: %s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">② 收货人 Consignee</div>
                                    <div>%s</div>
                                    %s
                                    <div>信用证号 L/C No: %s</div>
                                    <div>日期 Date: %s</div>
                                </div>
                            </div>
                            <div class="info-right">
                                <div class="info-box">
                                    <div class="label">⑧ 信用证号码及日期 L/C No. and date</div>
                                    <div>%s</div>
                                </div>
                                <div class="info-box">
                                    <div class="label">⑨ 买方 Buyer(if other than consignee)</div>
                                    <div>%s</div>
                                    <div>%s</div>
                                    %s
                                </div>
                                <div class="info-box">
                                    <div class="label">⑩ 其他参考事项 Other references</div>
                                    <div>%s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">③ 出发日期 Departure date</div>
                                    <div>%s</div>
                                </div>
                            </div>
                            <div class="info-right">
                                <div class="info-box">
                                    <div class="label">⑪ 交货及付款条件 Terms of delivery and payment</div>
                                    <div>交货条件 Terms: %s</div>
                                    <div>付款条件 Payment: %s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">④ 运输工具 Vessel/flight</div>
                                    <div>%s</div>
                                    <div class="label">⑤ 启运港/地 From</div>
                                    <div>%s</div>
                                </div>
                            </div>
                            <div class="info-right"></div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">⑥ 目的港/地 To</div>
                                    <div>%s</div>
                                </div>
                            </div>
                            <div class="info-right"></div>
                        </div>
                    </div>
                    
                    <table>
                        <thead>
                            <tr>
                                <th>⑫ 运输标志<br>Shipping Marks<br>%s</th>
                                <th>⑬ 包装种类及件数<br>No.&kind of packages</th>
                                <th>⑭ 货物描述<br>Goods description</th>
                                <th>⑮ 数量<br>Quantity</th>
                                <th>⑯ 单价<br>Unit price</th>
                                <th>⑰ 金额<br>Amount</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>
                    
                    <div style="text-align: right; margin-top: 20px;">
                        <div style="font-size: 16px; font-weight: bold;">
                            总金额 Total Amount: ¥%s
                        </div>
                    </div>
                    
                    <div class="signature-section">
                        <div class="signature-box">
                            <div class="label">签字 Signed by</div>
                            <div style="margin-top: 20px;">⑱ %s</div>
                        </div>
                    </div>
                </body>
                </html>
                """,
                esc(invoice.getShipperSellerName()),
                esc(invoice.getShipperSellerAddress()),
                invoice.getShipperSellerPhone() != null ? "<div>电话 Tel: " + esc(invoice.getShipperSellerPhone()) + "</div>" : "",
                esc(invoice.getInvoiceNumber()),
                invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                invoice.getConsigneeName() != null ? esc(invoice.getConsigneeName()) : "",
                invoice.getConsigneeAddress() != null ? "<div>" + esc(invoice.getConsigneeAddress()) + "</div>" : "",
                invoice.getLcNumber() != null ? esc(invoice.getLcNumber()) : "N/A",
                invoice.getLcDate() != null ? invoice.getLcDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
                invoice.getLcNumber() != null ? esc(invoice.getLcNumber()) : "N/A",
                esc(invoice.getBuyerName()),
                esc(invoice.getBuyerAddress()),
                invoice.getBuyerPhone() != null ? "<div>电话 Tel: " + esc(invoice.getBuyerPhone()) + "</div>" : "",
                invoice.getOtherReferences() != null ? esc(invoice.getOtherReferences()) : "N/A",
                invoice.getDepartureDate() != null ? invoice.getDepartureDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
                invoice.getTermsOfDelivery() != null ? esc(invoice.getTermsOfDelivery()) : "",
                invoice.getPaymentTerms() != null ? esc(invoice.getPaymentTerms()) : "",
                invoice.getVesselFlight() != null ? esc(invoice.getVesselFlight()) : "N/A",
                esc(invoice.getFromCountry()),
                invoice.getToDestination() != null ? esc(invoice.getToDestination()) : "N/A",
                invoice.getShippingMarks() != null ? esc(invoice.getShippingMarks()) : "N/A",
                itemsHtml.toString(),
                invoice.getTotalAmount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                esc(invoice.getShipperSellerName())
        );
    }

    private String generateJapanStandardHtml(CommercialInvoice invoice) {
        StringBuilder itemsHtml = new StringBuilder();
        
        for (CommercialInvoiceItem item : invoice.getItems()) {
            String hsCode = item.getHsCode() != null ? esc(item.getHsCode()) : "N/A";
            String countryOfOrigin = item.getCountryOfOrigin() != null ? esc(item.getCountryOfOrigin()) : "N/A";
            
            itemsHtml.append(String.format("""
                    <tr>
                        <td style="border: 1px solid black; padding: 5px; text-align: center;">%d</td>
                        <td style="border: 1px solid black; padding: 5px; text-align: center;">%s</td>
                        <td style="border: 1px solid black; padding: 5px;">%s<br><small>HSコード: %s<br>原産地: %s</small></td>
                        <td style="border: 1px solid black; padding: 5px; text-align: center;">%d</td>
                        <td style="border: 1px solid black; padding: 5px; text-align: right;">¥%s</td>
                        <td style="border: 1px solid black; padding: 5px; text-align: right;">¥%s</td>
                    </tr>
                    """,
                    item.getPackageCount(),
                    esc(item.getPackageType()),
                    esc(item.getGoodsDescription()),
                    hsCode,
                    countryOfOrigin,
                    item.getQuantity(),
                    item.getUnitPrice().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                    item.getAmount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
            ));
        }

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: "MS Gothic", Arial, sans-serif; margin: 20px; font-size: 12px; }
                        .header { text-align: center; font-size: 18px; font-weight: bold; margin-bottom: 20px; }
                        .header-jp { text-align: center; font-size: 16px; margin-bottom: 10px; }
                        .info-section { margin-bottom: 15px; }
                        .info-row { display: flex; margin-bottom: 10px; }
                        .info-left, .info-right { flex: 1; padding: 0 10px; }
                        .info-box { border: 1px solid black; padding: 8px; margin-bottom: 5px; }
                        .label { font-weight: bold; }
                        table { width: 100%%; border-collapse: collapse; margin: 15px 0; }
                        th, td { border: 1px solid black; padding: 5px; text-align: left; }
                        th { background-color: #f0f0f0; font-weight: bold; text-align: center; }
                        .signature-section { margin-top: 30px; text-align: right; }
                        .signature-box { border: 1px solid black; padding: 10px; width: 200px; margin-left: auto; }
                    </style>
                </head>
                <body>
                    <div class="header">商業インボイス COMMERCIAL INVOICE</div>
                    <div class="header-jp">輸出商品送り状</div>
                    
                    <div class="info-section">
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">① 荷送人/売主 Shipper/Seller</div>
                                    <div>%s</div>
                                    <div>%s</div>
                                    %s
                                </div>
                            </div>
                            <div class="info-right">
                                <div class="info-box">
                                    <div class="label">⑦ インボイス番号及び日付 Invoice No. and date</div>
                                    <div>インボイス番号 Invoice No: %s</div>
                                    <div>日付 Date: %s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">② 荷受人 Consignee</div>
                                    <div>%s</div>
                                    %s
                                    <div>信用状番号 L/C No: %s</div>
                                    <div>日付 Date: %s</div>
                                </div>
                            </div>
                            <div class="info-right">
                                <div class="info-box">
                                    <div class="label">⑧ 信用状番号及び日付 L/C No. and date</div>
                                    <div>%s</div>
                                </div>
                                <div class="info-box">
                                    <div class="label">⑨ 買主 Buyer(if other than consignee)</div>
                                    <div>%s</div>
                                    <div>%s</div>
                                    %s
                                </div>
                                <div class="info-box">
                                    <div class="label">⑩ その他の参照事項 Other references</div>
                                    <div>%s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">③ 出発日 Departure date</div>
                                    <div>%s</div>
                                </div>
                            </div>
                            <div class="info-right">
                                <div class="info-box">
                                    <div class="label">⑪ 引渡し及び支払い条件 Terms of delivery and payment</div>
                                    <div>引渡し条件 Terms: %s</div>
                                    <div>支払い条件 Payment: %s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">④ 船舶/航空便 Vessel/flight</div>
                                    <div>%s</div>
                                    <div class="label">⑤ 出発地 From</div>
                                    <div>%s</div>
                                </div>
                            </div>
                            <div class="info-right"></div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">⑥ 仕向地 To</div>
                                    <div>%s</div>
                                </div>
                            </div>
                            <div class="info-right"></div>
                        </div>
                    </div>
                    
                    <table>
                        <thead>
                            <tr>
                                <th>⑫ 船積み標記<br>Shipping Marks<br>%s</th>
                                <th>⑬ 包装の種類及び個数<br>No.&kind of packages</th>
                                <th>⑭ 貨物明細<br>Goods description</th>
                                <th>⑮ 数量<br>Quantity</th>
                                <th>⑯ 単価<br>Unit price</th>
                                <th>⑰ 金額<br>Amount</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>
                    
                    <div style="text-align: right; margin-top: 20px;">
                        <div style="font-size: 16px; font-weight: bold;">
                            合計金額 Total Amount: ¥%s
                        </div>
                    </div>
                    
                    <div class="signature-section">
                        <div class="signature-box">
                            <div class="label">署名 Signed by</div>
                            <div style="margin-top: 20px;">⑱ %s</div>
                        </div>
                    </div>
                </body>
                </html>
                """,
                esc(invoice.getShipperSellerName()),
                esc(invoice.getShipperSellerAddress()),
                invoice.getShipperSellerPhone() != null ? "<div>電話 Tel: " + esc(invoice.getShipperSellerPhone()) + "</div>" : "",
                esc(invoice.getInvoiceNumber()),
                invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                invoice.getConsigneeName() != null ? esc(invoice.getConsigneeName()) : "",
                invoice.getConsigneeAddress() != null ? "<div>" + esc(invoice.getConsigneeAddress()) + "</div>" : "",
                invoice.getLcNumber() != null ? esc(invoice.getLcNumber()) : "N/A",
                invoice.getLcDate() != null ? invoice.getLcDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
                invoice.getLcNumber() != null ? esc(invoice.getLcNumber()) : "N/A",
                esc(invoice.getBuyerName()),
                esc(invoice.getBuyerAddress()),
                invoice.getBuyerPhone() != null ? "<div>電話 Tel: " + esc(invoice.getBuyerPhone()) + "</div>" : "",
                invoice.getOtherReferences() != null ? esc(invoice.getOtherReferences()) : "N/A",
                invoice.getDepartureDate() != null ? invoice.getDepartureDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
                invoice.getTermsOfDelivery() != null ? esc(invoice.getTermsOfDelivery()) : "",
                invoice.getPaymentTerms() != null ? esc(invoice.getPaymentTerms()) : "",
                invoice.getVesselFlight() != null ? esc(invoice.getVesselFlight()) : "N/A",
                esc(invoice.getFromCountry()),
                invoice.getToDestination() != null ? esc(invoice.getToDestination()) : "N/A",
                invoice.getShippingMarks() != null ? esc(invoice.getShippingMarks()) : "N/A",
                itemsHtml.toString(),
                invoice.getTotalAmount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                esc(invoice.getShipperSellerName())
        );
    }

    private String generateEuStandardHtml(CommercialInvoice invoice) {
        StringBuilder itemsHtml = new StringBuilder();
        
        for (CommercialInvoiceItem item : invoice.getItems()) {
            String hsCode = item.getHsCode() != null ? esc(item.getHsCode()) : "N/A";
            String countryOfOrigin = item.getCountryOfOrigin() != null ? esc(item.getCountryOfOrigin()) : "N/A";
            
            itemsHtml.append(String.format("""
                    <tr>
                        <td style="border: 1px solid black; padding: 5px; text-align: center;">%d</td>
                        <td style="border: 1px solid black; padding: 5px; text-align: center;">%s</td>
                        <td style="border: 1px solid black; padding: 5px;">%s<br><small>HS Code: %s<br>Origin: %s</small></td>
                        <td style="border: 1px solid black; padding: 5px; text-align: center;">%d</td>
                        <td style="border: 1px solid black; padding: 5px; text-align: right;">€%s</td>
                        <td style="border: 1px solid black; padding: 5px; text-align: right;">€%s</td>
                    </tr>
                    """,
                    item.getPackageCount(),
                    esc(item.getPackageType()),
                    esc(item.getGoodsDescription()),
                    hsCode,
                    countryOfOrigin,
                    item.getQuantity(),
                    item.getUnitPrice().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                    item.getAmount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
            ));
        }

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; font-size: 12px; }
                        .header { text-align: center; font-size: 18px; font-weight: bold; margin-bottom: 20px; }
                        .header-eu { text-align: center; font-size: 16px; margin-bottom: 10px; }
                        .info-section { margin-bottom: 15px; }
                        .info-row { display: flex; margin-bottom: 10px; }
                        .info-left, .info-right { flex: 1; padding: 0 10px; }
                        .info-box { border: 1px solid black; padding: 8px; margin-bottom: 5px; }
                        .label { font-weight: bold; }
                        table { width: 100%%; border-collapse: collapse; margin: 15px 0; }
                        th, td { border: 1px solid black; padding: 5px; text-align: left; }
                        th { background-color: #f0f0f0; font-weight: bold; text-align: center; }
                        .signature-section { margin-top: 30px; text-align: right; }
                        .signature-box { border: 1px solid black; padding: 10px; width: 200px; margin-left: auto; }
                        .eu-compliance { text-align: center; font-size: 10px; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="header">COMMERCIAL INVOICE - EU FORMAT</div>
                    <div class="header-eu">European Union Export Documentation</div>
                    
                    <div class="info-section">
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">① Exporter/Seller</div>
                                    <div>%s</div>
                                    <div>%s</div>
                                    %s
                                </div>
                            </div>
                            <div class="info-right">
                                <div class="info-box">
                                    <div class="label">⑦ Invoice Number and Date</div>
                                    <div>Invoice No: %s</div>
                                    <div>Date: %s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">② Consignee</div>
                                    <div>%s</div>
                                    %s
                                    <div>L/C No: %s</div>
                                    <div>Date: %s</div>
                                </div>
                            </div>
                            <div class="info-right">
                                <div class="info-box">
                                    <div class="label">⑧ Letter of Credit No. and Date</div>
                                    <div>%s</div>
                                </div>
                                <div class="info-box">
                                    <div class="label">⑨ Buyer (if different from consignee)</div>
                                    <div>%s</div>
                                    <div>%s</div>
                                    %s
                                </div>
                                <div class="info-box">
                                    <div class="label">⑩ Other References</div>
                                    <div>%s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">③ Departure Date</div>
                                    <div>%s</div>
                                </div>
                            </div>
                            <div class="info-right">
                                <div class="info-box">
                                    <div class="label">⑪ Terms of Delivery and Payment</div>
                                    <div>Delivery Terms: %s</div>
                                    <div>Payment Terms: %s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">④ Transport Details</div>
                                    <div>%s</div>
                                    <div class="label">⑤ Country of Origin</div>
                                    <div>%s</div>
                                </div>
                            </div>
                            <div class="info-right"></div>
                        </div>
                        
                        <div class="info-row">
                            <div class="info-left">
                                <div class="info-box">
                                    <div class="label">⑥ Country of Destination</div>
                                    <div>%s</div>
                                </div>
                            </div>
                            <div class="info-right"></div>
                        </div>
                    </div>
                    
                    <table>
                        <thead>
                            <tr>
                                <th>⑫ Shipping Marks<br>%s</th>
                                <th>⑬ Number & Kind of Packages</th>
                                <th>⑭ Description of Goods</th>
                                <th>⑮ Quantity</th>
                                <th>⑯ Unit Price (EUR)</th>
                                <th>⑰ Total Amount (EUR)</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>
                    
                    <div style="text-align: right; margin-top: 20px;">
                        <div style="font-size: 16px; font-weight: bold;">
                            Total Invoice Amount: €%s
                        </div>
                    </div>
                    
                    <div class="signature-section">
                        <div class="signature-box">
                            <div class="label">Authorized Signature</div>
                            <div style="margin-top: 20px;">⑱ %s</div>
                        </div>
                    </div>
                    
                    <div class="eu-compliance">
                        <p>This invoice complies with EU Regulation requirements for export documentation.</p>
                        <p>All goods are in accordance with applicable EU standards and regulations.</p>
                    </div>
                </body>
                </html>
                """,
                esc(invoice.getShipperSellerName()),
                esc(invoice.getShipperSellerAddress()),
                invoice.getShipperSellerPhone() != null ? "<div>Tel: " + esc(invoice.getShipperSellerPhone()) + "</div>" : "",
                esc(invoice.getInvoiceNumber()),
                invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                invoice.getConsigneeName() != null ? esc(invoice.getConsigneeName()) : "",
                invoice.getConsigneeAddress() != null ? "<div>" + esc(invoice.getConsigneeAddress()) + "</div>" : "",
                invoice.getLcNumber() != null ? esc(invoice.getLcNumber()) : "N/A",
                invoice.getLcDate() != null ? invoice.getLcDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A",
                invoice.getLcNumber() != null ? esc(invoice.getLcNumber()) : "N/A",
                esc(invoice.getBuyerName()),
                esc(invoice.getBuyerAddress()),
                invoice.getBuyerPhone() != null ? "<div>Tel: " + esc(invoice.getBuyerPhone()) + "</div>" : "",
                invoice.getOtherReferences() != null ? esc(invoice.getOtherReferences()) : "N/A",
                invoice.getDepartureDate() != null ? invoice.getDepartureDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A",
                invoice.getTermsOfDelivery() != null ? esc(invoice.getTermsOfDelivery()) : "",
                invoice.getPaymentTerms() != null ? esc(invoice.getPaymentTerms()) : "",
                invoice.getVesselFlight() != null ? esc(invoice.getVesselFlight()) : "N/A",
                esc(invoice.getFromCountry()),
                invoice.getToDestination() != null ? esc(invoice.getToDestination()) : "N/A",
                invoice.getShippingMarks() != null ? esc(invoice.getShippingMarks()) : "N/A",
                itemsHtml.toString(),
                invoice.getTotalAmount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                esc(invoice.getShipperSellerName())
        );
    }
}