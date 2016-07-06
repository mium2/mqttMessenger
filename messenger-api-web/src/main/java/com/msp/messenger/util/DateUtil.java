package com.msp.messenger.util;


import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-06-23
 * Time: 오후 4:19
 * To change this template use File | Settings | File Templates.
 */
public class DateUtil implements Serializable {
    private static final long serialVersionUID = 2541140426147500192L;
    protected String year = null;		// 연도(4자리)
    protected String month = null;	// 월(2자리)
    protected String day = null;		// 일(2자리)

    protected String hour = null;		// 시(2자리)
    protected String minute = null;	// 분(2자리)
    protected String second = null;	// 초(2자리)

    protected String currentTime = null;	// 현재시간(14자리)
    protected String currentTime2 = "";

    public DateUtil(){
        // 현재시간을 구할 수 있는 객체 생성
        Calendar calendar = Calendar.getInstance();
        StringBuffer buffer = new StringBuffer();
        StringBuffer buffer2 = new StringBuffer();

        year = StringUtil.toZoroString( calendar.get(Calendar.YEAR) , 4 );
        month = StringUtil.toZoroString( calendar.get(Calendar.MONTH) + 1 , 2 );
        day = StringUtil.toZoroString( calendar.get(Calendar.DATE) , 2 );

        hour = StringUtil.toZoroString( calendar.get(Calendar.HOUR_OF_DAY) , 2 );
        minute = StringUtil.toZoroString( calendar.get(Calendar.MINUTE) , 2 );
        second = StringUtil.toZoroString( calendar.get(Calendar.SECOND) , 2 );

        buffer.append( year );          // 연도(4자리)
        buffer.append("-");
        buffer.append( month );         // 월(2자리)
        buffer.append("-");
        buffer.append( day );           // 일(2자리)
        buffer.append(" ");
        buffer.append( hour );          // 시(2자리)
        buffer.append(":");
        buffer.append( minute );        // 분(2자리)
        buffer.append(":");
        buffer.append( second );        // 초(2자리)
        currentTime = buffer.toString();	// 현재시간(14자리)

        buffer2.append( year );          // 연도(4자리)
        buffer2.append( month );         // 월(2자리)
        buffer2.append( day );           // 일(2자리)
        buffer2.append( " " );           // 한칸 띄움
        buffer2.append( hour );          // 시(2자리)
        buffer2.append( minute );        // 분(2자리)
        buffer2.append( second );        // 초(2자리)
        currentTime2 = buffer2.toString();
    }

    public static String getSysDateTime(){
        // 현재시간을 구할 수 있는 객체 생성
        Calendar calendar = Calendar.getInstance();
        StringBuffer buffer = new StringBuffer();

        String year = StringUtil.toZoroString( calendar.get(Calendar.YEAR) , 4 );
        String month = StringUtil.toZoroString( calendar.get(Calendar.MONTH) + 1 , 2 );
        String day = StringUtil.toZoroString( calendar.get(Calendar.DATE) , 2 );
        String hour = StringUtil.toZoroString( calendar.get(Calendar.HOUR_OF_DAY) , 2 );

        buffer.append(year);
        buffer.append(month);
        buffer.append(day);
        buffer.append(hour);

        return buffer.toString();
    }

    public static String getSysDay(){
        // 현재시간을 구할 수 있는 객체 생성
        Calendar calendar = Calendar.getInstance();
        StringBuffer buffer = new StringBuffer();

        String year = StringUtil.toZoroString( calendar.get(Calendar.YEAR) , 4 );
        String month = StringUtil.toZoroString( calendar.get(Calendar.MONTH) + 1 , 2 );
        String day = StringUtil.toZoroString( calendar.get(Calendar.DATE) , 2 );

        buffer.append(year);
        buffer.append(month);
        buffer.append(day);

        return buffer.toString();
    }

    // 연도 설정 및 반환
    public void setYear( String year )
    {
        int tempYear = Integer.parseInt(year);
        int temp = 0;

        try{
            if( tempYear < 2000 )
            {
                temp = 2000 + tempYear;
                this.year = String.valueOf(temp);
            }
            else
                this.year = year;
        }
        catch( Exception e)
        {
        }

    }
    public String getYear()
    {
        return year;
    }

    public int getYearInt()
    {
        return Integer.parseInt( year );
    }

    // 월 설정 및 반환
    public void setMonth( String month )
    {
        this.month = month;
    }
    public String getMonth()
    {
        return month;
    }
    public int getMonthInt()
    {
        return Integer.parseInt( month );
    }

    // 일 설정 및 반환
    public void setDay( String day )
    {
        this.day = day;
    }
    public String getDay()
    {
        return day;
    }
    public int getDayInt()
    {
        return Integer.parseInt(day);
    }

    // 시 설정 및 반환
    public void setHour( String hour )
    {
        this.hour = hour;
    }
    public String getHour()
    {
        return hour;
    }

    // 분 설정 및 반환
    public void setMinute( String minute )
    {
        this.minute = minute;
    }
    public String getMinute()
    {
        return minute;
    }

    // 초 설정 및 반환
    public void setSecond( String second )
    {
        this.second = second;
    }
    public String getSecond()
    {
        return second;
    }

    // 현재시간 반환
    public String getCurrentTime()
    {
        return currentTime;
    }
    //현재시간 숫자만 반환
    public String getCurrentTime2()
    {
        return currentTime2;
    }

    ///특정일에서 일정 기간후의 날짜 구하기
    public String getDate ( int iDay ,String strChar) {
        Calendar temp=Calendar.getInstance ();
        StringBuffer sbDate=new StringBuffer ();
        temp.add ( Calendar.DAY_OF_MONTH, iDay );
        int nYear = temp.get ( Calendar.YEAR );
        int nMonth = temp.get ( Calendar.MONTH ) + 1;
        int nDay = temp.get ( Calendar.DAY_OF_MONTH );

        sbDate.append ( nYear );
        if(!strChar.equals("")){sbDate.append (strChar);}

        if (nMonth < 10){sbDate.append ( "0" );}
        sbDate.append(nMonth);

        if(!strChar.equals("")){sbDate.append (strChar);}

        if (nDay < 10){sbDate.append ( "0" );}
        sbDate.append ( nDay );

        return sbDate.toString ();
    }
    ///오늘날짜 기준 요구달의 날짜 구하기
    public String getReqMonth ( int iMon ,String strChar) {
        Calendar cal = Calendar.getInstance ();//오늘 날짜를 기준으루..
        StringBuffer sbDate = new StringBuffer();
        cal.add (Calendar.MONTH, iMon ); //iMon 요구달 (-1일경우 일개월 전....)
        int nYear = cal.get ( Calendar.YEAR );
        int nMonth = cal.get ( Calendar.MONTH ) + 1;
        int nDay = cal.get ( Calendar.DATE );

        sbDate.append ( nYear );
        if(!strChar.equals("")){sbDate.append (strChar);}

        if (nMonth < 10){sbDate.append ( "0" );}
        sbDate.append(nMonth);
        if(!strChar.equals("")){sbDate.append (strChar);}

        if (nDay < 10){sbDate.append ( "0" );}
        sbDate.append ( nDay );

        return sbDate.toString ();
    }

    ///오늘 날짜와 비교
    public int CompareDate(int year, int month, int day, int time, int minute, int second){    /// 0 작고 1일면 같고 2이면 크다
        int returnValue=0;
        Calendar adate = Calendar.getInstance();
        Calendar bdate = Calendar.getInstance();
        bdate.set(year, month, day, time, minute, second);
        if(adate.after(bdate)){
            returnValue = 2;
        }else if(adate.before(bdate)){
            returnValue = 0;
        }else{
            returnValue = 1;
        }
        return returnValue;
    }

    ///날짜와 비교
    public static int CompareDate2(int year, int month, int day, int time, int minute, int second,Calendar b){    /// 0 : A날보다 B날자가 전, 1일면 같고 2 : A날보다 B날자가 후
        int returnValue=0;
        Calendar adate = Calendar.getInstance();
        Calendar bdate = b;
        adate.set(year, month - 1, day, time, minute, second);

        if(adate.after(bdate)){
            returnValue = 2;
        }else if(adate.before(bdate)){
            returnValue = 0;
        }else{
            returnValue = 1;
        }
        return returnValue;
    }

    // 만료제한시간 타임스템프 만들기.
    // 주의 넘어온 시간이 현재 시간보다 작을 경우 익일로 세팅한다.(하루 밀리세컨드 24*60*60*1000)
    public static long getMakeTimeStamp(int time, int minute){
        Calendar adate = Calendar.getInstance();
        int now_year = adate.get(Calendar.YEAR);
        int now_month = adate.get(Calendar.MONTH);
        int now_day = adate.get(Calendar.DATE);
        int now_hour = adate.get(Calendar.HOUR);
        int now_minute = adate.get(Calendar.MINUTE);
//        System.out.println("### YEAR:" + now_year + "  MONTH:" + now_month + "   DATE:" + now_day+"    HOUR:"+now_hour+"  하루 milsecond:"+24*60*60*1000);
        adate.set(now_year, now_month, now_day, time, minute,0);
        long returnTimeMilis = adate.getTimeInMillis();
//        System.out.println("하루더하기 전:"+returnTimeMilis);
        if(now_hour>time){ // 요청시간이 현재시간보다 작을 경우 다음날로 처리해야함
            returnTimeMilis = returnTimeMilis+(24*60*60*1000);
//            System.out.println("하루더한 후 :"+returnTimeMilis);
        }else if(now_hour==time){
            if(now_minute>minute){
                returnTimeMilis = returnTimeMilis+(24*60*60*1000);
//                System.out.println("하루더한 후 :"+returnTimeMilis);
            }
        }
//        System.out.println("### make milis:"+returnTimeMilis+"      now mils:"+ System.currentTimeMillis());
        return returnTimeMilis;
    }

    public static int GetDifferenceOfDate ( int nYear1, int nMonth1, int nDate1, int nYear2, int nMonth2, int nDate2 ){
        Calendar cal = Calendar.getInstance ( );
        int nTotalDate1 = 0, nTotalDate2 = 0, nDiffOfYear = 0, nDiffOfDay = 0;
        if ( nYear1 > nYear2 ){
            for ( int i = nYear2; i < nYear1; i++ ) {
                cal.set ( i, 12, 0 );
                nDiffOfYear += cal.get ( Calendar.DAY_OF_YEAR );
            }
            nTotalDate1 += nDiffOfYear;
        }else if ( nYear1 < nYear2 ){
            for ( int i = nYear1; i < nYear2; i++ ){
                cal.set ( i, 12, 0 );
                nDiffOfYear += cal.get ( Calendar.DAY_OF_YEAR );
            }
            nTotalDate2 += nDiffOfYear;
        }
        cal.set ( nYear1, nMonth1-1, nDate1 );
        nDiffOfDay = cal.get ( Calendar.DAY_OF_YEAR );
        nTotalDate1 += nDiffOfDay;
        cal.set ( nYear2, nMonth2-1, nDate2 );
        nDiffOfDay = cal.get ( Calendar.DAY_OF_YEAR );
        nTotalDate2 += nDiffOfDay;
        return nTotalDate1-nTotalDate2;
    }
}
