import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-12-05
 * Time: 오전 10:52
 * To change this template use File | Settings | File Templates.
 */
public class TestSendWork {

    private int SENDMSG_SEQNO = 0;
    private String CUID = "";
    private String CNAME = "";
    private String PHONENO = "";
    private String APP_ID = "";
    private String PSID = "";
    private String MESSAGE = "";

    public int getSENDMSG_SEQNO() {
        return SENDMSG_SEQNO;
    }

    public void setSENDMSG_SEQNO(int SENDMSG_SEQNO) {
        this.SENDMSG_SEQNO = SENDMSG_SEQNO;
    }

    public String getCUID() {
        return CUID;
    }

    public void setCUID(String CUID) {
        this.CUID = CUID;
    }

    public String getCNAME() {
        return CNAME;
    }

    public void setCNAME(String CNAME) {
        this.CNAME = CNAME;
    }

    public String getPHONENO() {
        return PHONENO;
    }

    public void setPHONENO(String PHONENO) {
        this.PHONENO = PHONENO;
    }

    public String getAPP_ID() {
        return APP_ID;
    }

    public void setAPP_ID(String APP_ID) {
        this.APP_ID = APP_ID;
    }

    public String getPSID() {
        return PSID;
    }

    public void setPSID(String PSID) {
        this.PSID = PSID;
    }

    public String getMESSAGE() {
        return MESSAGE;
    }

    public void setMESSAGE(String MESSAGE) {
        this.MESSAGE = MESSAGE;
    }

    public void workExecute() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("##########["+dateFormat.format(calendar.getTime())+"] CUID:" + CUID);
    }
}
