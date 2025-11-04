package com.ptit.common.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// Định dạng giá tiền (Int) thành chuỗi có dấu chấm phân cách hàng nghìn
fun Int.toPriceFormat(): String {
    // 1. Định nghĩa ký hiệu phân cách
    val symbols = DecimalFormatSymbols(Locale.getDefault())

    // Sử dụng dấu chấm (.) làm dấu phân cách hàng nghìn (Grouping Separator)
    symbols.groupingSeparator = '.'

    // 2. Định dạng: "#,##0" sẽ sử dụng dấu phân cách hàng nghìn đã định nghĩa
    val formatter = DecimalFormat("#,##0", symbols)

    // 3. Định dạng và thêm đơn vị tiền tệ " đ"
    return "${formatter.format(this)} đ"
}
