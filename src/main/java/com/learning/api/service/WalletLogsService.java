package com.learning.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.learning.api.dto.EcpayReturnDto;
import com.learning.api.entity.User;
import com.learning.api.entity.WalletLog;
import com.learning.api.repo.UserRepo;
import com.learning.api.repo.WalletLogsRepo;

import jakarta.transaction.Transactional;

import java.util.List;

@Service
public class WalletLogsService {

	@Autowired
    private WalletLogsRepo walletLogsRepo;
	@Autowired
    private UserRepo usersRepo;

    @Transactional
    public void processWalletDeposit(EcpayReturnDto dto) {
        // 1. 取得使用者
        Long userId = Long.parseLong(dto.getCustomField1());
        User user = usersRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("找不到使用者 ID: " + userId));
        // 直接解析為 int
        int ecpayprice = Integer.parseInt(dto.getTradeAmt());

        int wallet = (user.getWallet() != null) ? user.getWallet() : 0;
       
        WalletLog log = new WalletLog();
        if(ecpayprice==1500) {
        	user.setWallet(wallet + ecpayprice + 200);
        	log.setDType(1);
        }else if (ecpayprice==3000) {
        	user.setWallet(wallet + ecpayprice + 600);
        	log.setDType(2);
        }else if (ecpayprice==5000) {
        	user.setWallet(wallet + ecpayprice + 1200);
        	log.setDType(3);
        }else {
            throw new IllegalArgumentException("不支援的儲值金額: " + ecpayprice);
        }
        // 2. 建立錢包日誌
        log.setUserId(user.getId());
        log.setAmount(Long.parseLong(dto.getTradeAmt()));
        log.setTransactionType(1); // 假設 1: 儲值, 2: 消費
        log.setMerchantTradeNo(dto.getMerchantTradeNo());
        
        // 如果有相關類型需求可設定，如：儲值來自綠界
        log.setRelatedType(3); 

        walletLogsRepo.save(log);
    }

    public List<WalletLog> getLogsByUserId(Long userId) {
        return walletLogsRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
}

