package com.msp.messenger.common.modelview;

import com.msp.messenger.util.ContentTypeUtil;
import org.apache.log4j.Logger;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 16. 8. 5..
 */
public class DownLoadView extends AbstractView {

    private String Req_ContentType = "application/octet-stream";
    private File SendDownFile = null;

    public DownLoadView(File downFile, String extention) {
        this.Req_ContentType = ContentTypeUtil.getContentType(extention);
        SendDownFile = downFile;
        super.setContentType(Req_ContentType);
    }

    protected void renderMergedOutputModel(Map model,HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        FileInputStream lm_oInputStream = null;
        ServletOutputStream lm_oOutStream = null;

        try {
            lm_oInputStream = new FileInputStream(SendDownFile);
            long lm_longFileLength = SendDownFile.length();
            logger.info("#### Response Content-Type:"+Req_ContentType);
            response.setHeader("Content-Disposition", "attachment; filename=" + new String(SendDownFile.getName().getBytes(), response.getCharacterEncoding()));
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setContentLength((int)lm_longFileLength);
            response.setHeader("Content-Type", Req_ContentType);

            lm_oOutStream = response.getOutputStream();
            FileCopyUtils.copy(lm_oInputStream, lm_oOutStream);
        } catch(Exception e) {
            logger.error(e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } finally {
            if (lm_oInputStream != null) try { lm_oInputStream.close();} catch (IOException e) {}
            if (lm_oOutStream != null) try { lm_oOutStream.close();} catch (IOException e) {}
        }
    }
}
