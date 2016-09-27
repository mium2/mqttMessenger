package com.msp.messenger.controller;

import com.msp.messenger.common.modelview.DownLoadView;
import com.msp.messenger.common.modelview.ImageModelView;
import com.msp.messenger.util.FileDownloadAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 16. 8. 5..
 */
@Controller
public class DownloadController {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Value("${file.upload.dir}")
    private String UPLOAD_ROOT_PATH;

    @RequestMapping(value = "/download.ctl", method= RequestMethod.GET)
    public ModelAndView get(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String accToken = request.getParameter("accToken");
        String subDir = request.getParameter("sub");
        Map<String,String> valideCheckMap = FileDownloadAuthUtils.getInstance().validateDownloadToken(accToken);

        if(!valideCheckMap.get("resultcode").equals("200")){
            response.setStatus(403);
            response.sendError(403,valideCheckMap.get("resultmsg"));
            return null;
        }

        String chatroomid = valideCheckMap.get("chatroomid");
        String ext = valideCheckMap.get("ext");
        String publisher = valideCheckMap.get("publisher");
        String filename = valideCheckMap.get("filename");

        StringBuilder fileDirSB = new StringBuilder(UPLOAD_ROOT_PATH + File.separator + chatroomid);
        if(subDir.equals("1")){
            fileDirSB.append(File.separator+"thumb");
        }
        fileDirSB.append(File.separator+filename);

        File lm_oAttachFile = new File(fileDirSB.toString());
        if(FileDownloadAuthUtils.getInstance().chkImageFile(ext)){
            ModelAndView mv = new ModelAndView();
            mv.setView(new ImageModelView(lm_oAttachFile,ext));
            return mv;
        }else{
            ModelAndView mv = new ModelAndView();
            mv.setView(new DownLoadView(lm_oAttachFile,ext));
            return mv;
        }

    }
}
