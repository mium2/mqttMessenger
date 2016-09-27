package com.msp.messenger.controller;

import com.msp.messenger.common.Constants;
import com.msp.messenger.util.FileDownloadAuthUtils;
import com.msp.messenger.util.JsonObjectConverter;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by Y.B.H(mium2) on 16. 8. 5..
 */
@Controller
public class UploadController {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Value("${file.upload.dir}")
    private String UPLOAD_ROOT_PATH;
    @Value("${file.download.root.url}")
    private String DOWNLOAD_ROOT_URL;
    @Value("${thumbnail.height:100}")
    private String THUMBNAIL_HEIGHT;

    @RequestMapping(value="/fileUpload.ctl", method= RequestMethod.POST ,produces = "application/json; charset=utf8")
    public @ResponseBody
    String fileUpload(HttpServletRequest request, HttpServletResponse response){

        Map<String,String> resultHeadMap = new HashMap<String, String>();
        resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.RESULT_CODE_OK);
        resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.RESULT_MESSAGE_OK);
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        String req_APPID = "";
        String req_ROOMID = "";
        try{
            // ###################### 토큰 인증 결과 처리 시작 #############################
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            //인증에러가 아닐 경우만 비즈니스 로직 수행
            if (!resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) { // 토큰인증체크
                return responseJson(resultHeadMap, resultBodyMap, req_APPID); // 인증 실패 처리
            }
            String userId = resultHeadMap.get("USERID");
            resultHeadMap.remove("USERID"); //토큰에서 추출한 정보를 응답데이터에 넘기지 않기 위해
            // ###################### 토큰 인증결과 처리 마침 ###############################

            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if(isMultipart){
                List<Map<String,String> > downloadUrls = new ArrayList<Map<String,String> >();
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

                req_APPID = multipartRequest.getParameter("APPID");
                req_ROOMID = multipartRequest.getParameter("ROOMID");

                if(req_APPID==null || req_ROOMID==null || "".equals(req_APPID) || "".equals(req_ROOMID)){
                    resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
                    return responseJson(resultHeadMap,resultBodyMap, req_APPID);
                }

                final Map<String, MultipartFile> files = multipartRequest.getFileMap();
                Iterator<Map.Entry<String, MultipartFile>> itr = files.entrySet().iterator();
                MultipartFile file;
                String filePath = "";
                String uploadFileName = "";
                String fileExtention = "";
                while (itr.hasNext()) {
                    Map.Entry<String, MultipartFile> entry = itr.next();
                    file = entry.getValue();
                    String orgFileName = file.getOriginalFilename();
                    if(orgFileName.trim().equals("")){
                        continue;
                    }
                    fileExtention = orgFileName.substring(orgFileName.lastIndexOf(".")+1,orgFileName.length()).toLowerCase();
                    uploadFileName = System.currentTimeMillis()+"."+fileExtention;

                    filePath = UPLOAD_ROOT_PATH + req_ROOMID;
                    FileUtils.forceMkdir(new File(filePath));
                    File saveFile = new File(filePath + File.separator + uploadFileName);
                    logger.debug("## save file Path:"+saveFile);
                    file.transferTo(saveFile);

                    // 서브디렉토리명은 챗팅방아이디로 만드는게 관리 상 좋아 보임. 해당 첨부파일은 메세지가 삭제될때 같이 삭제되도록 처리함.
                    //확장자,파일명,챗팅방아이디,발송자아이디를 가지고
                    String fileAccessToken = FileDownloadAuthUtils.getInstance().makeDownloadToken(userId, uploadFileName,fileExtention,req_ROOMID);
                    Map<String,String> imgInfoMap = new HashMap<String, String>();
                    imgInfoMap.put("downloadUrl", DOWNLOAD_ROOT_URL + "download.ctl" + "?accToken=" + fileAccessToken + "&sub=0");

                    //thumbnail 이미지 만들기
                    if(FileDownloadAuthUtils.getInstance().chkImageFile(fileExtention)){
                        String thumbFileDir = UPLOAD_ROOT_PATH + req_ROOMID + File.separator + "thumb";
                        FileUtils.forceMkdir(new File(thumbFileDir));
                        String thumbFilePath = UPLOAD_ROOT_PATH + req_ROOMID + File.separator + "thumb" + File.separator + uploadFileName;
                        File thumbFile = new File(thumbFilePath);
                        BufferedImage buffer_original_image = ImageIO.read(saveFile);
                        int orgImgHeight = buffer_original_image.getHeight();
                        int orgImgWidth = buffer_original_image.getWidth();
                        int thumbHeight = Integer.parseInt(THUMBNAIL_HEIGHT);

                        int autoResizeWidth = (orgImgWidth * thumbHeight) / orgImgHeight;
                        BufferedImage buffer_thumbnail_image;
                        if (fileExtention.equals("png")) {
                            buffer_thumbnail_image = new BufferedImage(autoResizeWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB);
                        } else {
                            buffer_thumbnail_image = new BufferedImage(autoResizeWidth, thumbHeight, BufferedImage.TYPE_3BYTE_BGR);
                        }
                        Graphics2D graphic = buffer_thumbnail_image.createGraphics();
                        graphic.drawImage(buffer_original_image, 0, 0, autoResizeWidth, thumbHeight, null);
                        ImageIO.write(buffer_thumbnail_image, fileExtention, thumbFile);
                        imgInfoMap.put("thumbnailUrl", DOWNLOAD_ROOT_URL + "download.ctl" + "?accToken=" + fileAccessToken + "&sub=1");
                    }

                    downloadUrls.add(imgInfoMap);
                    resultBodyMap.put("uploadInfo",downloadUrls);
                }

            }else{
                resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
                resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, "ENCTYPE이 multipart/form-data가 아닙니다.");
                return responseJson(resultHeadMap,resultBodyMap, request.getParameter("APPID"));
            }

            /**************************************************************************************************
             * 이 부분에 비즈니스 로직 마침.
             *************************************************************************************************/
        } catch (Exception e) {
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap,resultBodyMap, request.getParameter("APPID"));
        }
        return responseJson(resultHeadMap, resultBodyMap, req_APPID);
    }

    private String responseJson(Map<String,String> resultHeadMap,Map<String,Object> resultBodyMap,String reqAPPID){
        Map<String,Object> returnRootMap = new HashMap<String, Object>();
        returnRootMap.put(Constants.HEADER_KEY, resultHeadMap);
        returnRootMap.put(Constants.BODY_KEY, resultBodyMap);
        returnRootMap.put(Constants.HEADER_SERVICE, reqAPPID);

        String responseJson = JsonObjectConverter.getJSONFromObject(returnRootMap);
        responseJson = "myCallback("+responseJson+")";
        logger.debug("###[UploadController] :" + responseJson);
        return responseJson;
    }
}
