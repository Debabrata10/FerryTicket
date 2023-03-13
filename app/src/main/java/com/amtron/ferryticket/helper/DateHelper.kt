package com.amtron.ferryticket.helper

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
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

	//For API 26 and above
	/*fun getTodayOrTomorrow(req: String, format: String): String {
		val current = LocalDateTime.now()
		var output = ""
		if (req == "today") {
			if (format == "dd-MM-yyyy") {
				val f = DateTimeFormatter.ofPattern("dd-MM-yyyy")
				output = current.format(f)
			} else if (format == "dd MMM, yyyy") {
				val f = DateTimeFormatter.ofPattern("dd MMM, yyyy")
				output = current.format(f)
			}
		} else if (req == "tomorrow") {
			if (format == "dd-MM-yyyy") {
				val f = DateTimeFormatter.ofPattern("dd-MM-yyyy")
				output = current.format(f).plus(1)
			} else if (format == "dd MMM, yyyy") {
				val f = DateTimeFormatter.ofPattern("dd MMM, yyyy")
				output = current.format(f).plus(1)
			}
		}
		return output
	}*/

	@SuppressLint("SimpleDateFormat")
	fun getNow(): String {
		val time = Calendar.getInstance().time
		val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm")
		return formatter.format(time)
	}

	@SuppressLint("SimpleDateFormat")
	fun getToday(format: String): String {
		val time = Calendar.getInstance().time
		var output = ""
		if (format == "dd MMM, yyyy") {
			val formatter = SimpleDateFormat("dd MMM, yyyy")
			output = formatter.format(time)
		} else if (format == "dd-MM-yyyy") {
			val formatter = SimpleDateFormat("dd-MM-yyyy")
			output = formatter.format(time)
		}
		return output
	}
}