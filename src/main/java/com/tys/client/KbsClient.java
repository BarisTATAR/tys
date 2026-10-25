package com.tys.client;

public class KbsClient {
    SrvShsYtkTml service = new SrvShsYtkTml();
    ISrvShsYtkTml port = service.getBasicHttpsBindingKBSServis();
}
