package jiguang.chat.utils;

import android.content.Context;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import cn.jiguang.api.JCoreInterface;


public class TimeFormat {

    private long mTimeStamp;
    private Context mContext;

    public TimeFormat(Context context, long timeStamp) {
        this.mContext = context;
        this.mTimeStamp = timeStamp;
        //yyyy-MM-dd HH:mm
        SimpleDateFormat format = new SimpleDateFormat(mContext.getString(IdHelper.getString(mContext, "jmui_time_format_accuracy")), Locale.CHINA);
        String date = format.format(timeStamp);
    }

    //用于显示会话时间
    public String getTime() {
        long currentTime = JCoreInterface.getReportTime();
        Date date1 = new Date(currentTime * 1000);
        Date date2 = new Date(mTimeStamp);
        //HH:mm
        SimpleDateFormat format = new SimpleDateFormat(mContext.getString(IdHelper.getString(mContext, "jmui_time_format_hours")), Locale.CHINA);
        //yyyy-MM-dd HH:mm
        SimpleDateFormat format1 = new SimpleDateFormat(mContext.getString(IdHelper.getString(mContext, "jmui_time_format_accuracy")), Locale.CHINA);
        String date = format.format(mTimeStamp);
        int hour = Integer.parseInt(date.substring(0, 2));
        //今天
        if (date1.getDate() - date2.getDate() == 0) {
            return date;
            //昨天
        } else if (date1.getDate() - date2.getDate() == 1) {
            return mContext.getString(IdHelper.getString(mContext, "jmui_yesterday"));
        } else if (date1.getDay() - date2.getDay() > 0) {
            switch (date2.getDay()) {
                case 1://周一
                    return mContext.getString(IdHelper.getString(mContext, "jmui_monday"));
                case 2:
                    return mContext.getString(IdHelper.getString(mContext, "jmui_tuesday"));
                case 3:
                    return mContext.getString(IdHelper.getString(mContext, "jmui_wednesday"));
                case 4:
                    return mContext.getString(IdHelper.getString(mContext, "jmui_thursday"));
                case 5:
                    return mContext.getString(IdHelper.getString(mContext, "jmui_friday"));
                case 6:
                    return mContext.getString(IdHelper.getString(mContext, "jmui_saturday"));
                default:
                    return mContext.getString(IdHelper.getString(mContext, "jmui_sunday"));
            }
        } else if (date1.getYear() == date2.getYear()) {
            //月
            return date2.getMonth() + 1 + mContext.getString(IdHelper.getString(mContext, "jmui_month"))
                    //日
                    + date2.getDate() + mContext.getString(IdHelper.getString(mContext, "jmui_day"));
        } else {
            return format1.format(mTimeStamp);
        }
    }

    //用于显示消息具体时间
    public String getDetailTime() {
        long currentTime = JCoreInterface.getReportTime();
        Date date1 = new Date(currentTime * 1000);
        Date date2 = new Date(mTimeStamp);

        long time = currentTime * 1000 - mTimeStamp;
        long days = time / (24 * 60 * 60 * 1000);
        //HH:mm
        SimpleDateFormat format = new SimpleDateFormat(mContext.getString(IdHelper.getString(mContext, "jmui_time_format_hours")), Locale.CHINA);
        String date = format.format(mTimeStamp);
        if (date1.getDate() - date2.getDate() == 0) {
            return date;
        } else if (days > 0 && days <= 1) {
            return mContext.getString(IdHelper.getString(mContext, "jmui_yesterday"));
        } else if (days > 1 && days <= 2) {
            return mContext.getString(IdHelper.getString(mContext, "jmui_before_yesterday"));
        } else if (days >= 3 && days <= 7) {
            switch (date2.getDay()) {
                case 1://周一
                    return mContext.getString(IdHelper.getString(mContext, "jmui_monday"));
                case 2:
                    return mContext.getString(IdHelper.getString(mContext, "jmui_tuesday"));
                case 3:
                    return mContext.getString(IdHelper.getString(mContext, "jmui_wednesday"));
                case 4:
                    return mContext.getString(IdHelper.getString(mContext, "jmui_thursday"));
                case 5:
                    return mContext.getString(IdHelper.getString(mContext, "jmui_friday"));
                case 6:
                    return mContext.getString(IdHelper.getString(mContext, "jmui_saturday"));
                default:
                    return mContext.getString(IdHelper.getString(mContext, "jmui_sunday"));
            }
        } else if (date1.getYear() == date2.getYear()) {
            return date2.getMonth() + 1 + mContext.getString(IdHelper.getString(mContext, "jmui_month"))
                    + date2.getDate() + mContext.getString(IdHelper.getString(mContext, "jmui_day"));
        } else {
            return date2.getYear() + "年 " + date2.getMonth() + 1 + mContext.getString(IdHelper.getString(mContext, "jmui_month"))
                    + date2.getDate() + mContext.getString(IdHelper.getString(mContext, "jmui_day"));
        }
    }
}
