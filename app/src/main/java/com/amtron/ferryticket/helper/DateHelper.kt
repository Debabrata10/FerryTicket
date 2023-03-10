package com.amtron.ferryticket.helper

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DateHelper {
	@SuppressLint("SimpleDateFormat")
	fun changeDateFormat(requiredFormat: String?, dateString: String?): String {
		val result: String

		val formatterOld = SimpleDateFormat("yyyy-mm-dd")
		val formatterNew = SimpleDateFormat(requiredFormat, Locale.getDefault())
		val date: Date = formatterOld.parse(dateString!!) as Date
		result = formatterNew.format(date)
		return result
	}

	fun getTodayOrTomorrow(req: String, format: String): String {
//		val current = LocalDateTime.now()
		val date = Date()
		val current = SimpleDateFormat(format, Locale.getDefault())
		var output = ""
		if (req == "today") {
			if (format == "dd-MM-yyyy") {
//				val f = DateTimeFormatter.ofPattern("dd-MM-yyyy")
				val f = current.format(date)
				output = current.format(f)
			} else if (format == "dd MMM, yyyy") {
//				val f = DateTimeFormatter.ofPattern("dd MMM, yyyy")
				val f = current.format(date)
				output = current.format(f)
			}
		} else if (req == "tomorrow") {
			if (format == "dd-MM-yyyy") {
//				val f = DateTimeFormatter.ofPattern("dd-MM-yyyy")
				val f = current.format(date)
				output = current.format(f).plus(1)
			} else if (format == "dd MMM, yyyy") {
//				val f = DateTimeFormatter.ofPattern("dd MMM, yyyy")
				val f = current.format(date)
				output = current.format(f).plus(1)
			}
		}
		return output
	}

	@SuppressLint("SimpleDateFormat")
	fun getNow(): String {
		val time = Calendar.getInstance().time
		val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm")
		return formatter.format(time)
	}
}