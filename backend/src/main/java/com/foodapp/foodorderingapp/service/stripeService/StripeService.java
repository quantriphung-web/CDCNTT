package com.foodapp.foodorderingapp.service.stripeService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Charge;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

@Service
public class StripeService {

    @Value("${STRIPE_SECRET_KEY}")
    private String secretKey;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
   
    
    public String createConnectedAccount(String email) throws StripeException  {
         Stripe.apiKey = secretKey;

        // Tạo yêu cầu tạo tài khoản
        AccountCreateParams params = AccountCreateParams.builder()
                .setCountry("US")
                .setType(AccountCreateParams.Type.EXPRESS)
                .setEmail(email)
                .setCapabilities(
                        AccountCreateParams.Capabilities.builder()
                                .setCardPayments(
                                        AccountCreateParams.Capabilities.CardPayments.builder()
                                                .setRequested(true)
                                                .build()
                                )
                                .setTransfers(
                                        AccountCreateParams.Capabilities.Transfers.builder()
                                                .setRequested(true)
                                                .build()
                                )
                                .build()
                )
                .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)  
                .build();


                Account account = Account.create(params);
                return account.getId();
    }
     public String generateAccountLink(String connectedAccountId) throws Exception {
        Stripe.apiKey = secretKey;
        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(connectedAccountId) // Connected account ID
                .setRefreshUrl("https://food-delivery-web-beryl.vercel.app") // URL to redirect if the user abandons the process
                .setReturnUrl("https://food-delivery-web-beryl.vercel.app") // URL to redirect after successful onboarding
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING) // Type of onboarding
                .build();

        AccountLink accountLink = AccountLink.create(params);

        // Return the URL
        return accountLink.getUrl();
    }
}