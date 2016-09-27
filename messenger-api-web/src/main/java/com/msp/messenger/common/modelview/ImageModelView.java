package com.msp.messenger.common.modelview;

import com.msp.messenger.util.ContentTypeUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 16. 8. 5..
 */
public class ImageModelView extends AbstractView {
    private File responseImgFile = null;
    private String Req_ContentType = "image/png";
    public ImageModelView(File imgFile, String extention) {
        this.responseImgFile = imgFile;
        if(extention!=null && !extention.trim().equals("")){
            Req_ContentType = ContentTypeUtil.getContentType(extention);
        }
        setContentType(Req_ContentType);
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ByteArrayOutputStream baos = createTemporaryOutputStream();
        byte[] img = FileUtils.readFileToByteArray(responseImgFile);
        baos.write(img, 0, img.length);
        baos.close();
        writeToResponse(response, baos);
    }
}
