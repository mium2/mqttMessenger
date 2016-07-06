package com.msp.messenger.auth;

import com.msp.messenger.auth.vo.ClientVO;
import com.msp.messenger.auth.vo.TokenVO;
import com.msp.messenger.auth.vo.UserVO;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Y.B.H(mium2) on 16. 6. 20..
 */
@Component
public class MemoryTokenStorage {
    //Key : CLIENT_ID
    private static Map<String,ClientVO> clientVOMap = new ConcurrentHashMap<String, ClientVO>();

    //KEY : CLIENT_ID + USERID
    private static Map<String,TokenVO> tokenVOMap = new ConcurrentHashMap<String, TokenVO>();

    public List<ClientVO> getClientList(UserVO vo) throws Exception {
        List<ClientVO> clientVOs = new ArrayList<ClientVO>();
        Set<Map.Entry<String,ClientVO>> mapSet = clientVOMap.entrySet();
        for(Map.Entry<String,ClientVO> mapEntry : mapSet){
            ClientVO clientVO = mapEntry.getValue();
            if(clientVO.getUSERID().equals(vo.getUSERID())){
                clientVOs.add(clientVO);
            }
        }
        return clientVOs;
    }

    public ClientVO getClientOne(ClientVO vo) throws Exception {
        return clientVOMap.get(vo.getCLIENT_ID());
    }

    public synchronized void deleteClient(ClientVO vo) throws Exception {
        clientVOMap.remove(vo.getCLIENT_ID());
    }

    public synchronized void insertClient(ClientVO vo) throws Exception {
        clientVOMap.put(vo.getCLIENT_ID(), vo);
    }

    public void createToken(TokenVO vo) throws Exception {
        String tokenKey = vo.getCLIENT_ID()+"_"+vo.getUSERID();
        tokenVOMap.put(tokenKey,vo);
    }

    public TokenVO selectRefreshToken(TokenVO vo) throws Exception {
        return tokenVOMap.get(vo.getCLIENT_ID()+"_"+vo.getUSERID());
    }

    public TokenVO selectToken(TokenVO vo) throws Exception {
        return null;
    }

    public TokenVO selectTokenByCode(TokenVO vo) throws Exception {
        return null;
    }

    public TokenVO chkToken(TokenVO vo) throws Exception {
        return tokenVOMap.get(vo.getCLIENT_ID()+"_"+vo.getUSERID());
    }

    public void upAccessTokenByID(TokenVO vo) throws Exception {
        tokenVOMap.put(vo.getCLIENT_ID()+"_"+vo.getUSERID(),vo);
    }

    public void upAccessTokenByRefreshToken(TokenVO vo) throws Exception {

    }

    public void deleteExpiredToken(TokenVO vo) throws Exception {

    }

    public void deleteToken(TokenVO vo) throws Exception {

    }

    public void deleteExpiredToken(long ms) throws Exception {

    }
}
