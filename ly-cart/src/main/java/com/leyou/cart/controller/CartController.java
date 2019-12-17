package com.leyou.cart.controller;

import com.leyou.cart.entity.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        this.cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询购物车中的信息
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Cart>> listCarts(){
        return ResponseEntity.ok(this.cartService.listCarts());
    }


    @PostMapping("list")
    public ResponseEntity<Void> patchAddCarts(@RequestBody List<Cart> carts){

        this.cartService.patchAddCarts(carts);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改购物车商品的数量（增或减）
     * @param skuId
     * @param num
     * @return
     */
    @PutMapping()
    public ResponseEntity<Void> modifyCartNum(
            @RequestParam("id")Long skuId,
            @RequestParam("num")Integer num
    ){
        this.cartService.modifyCartNum(skuId,num);
        return ResponseEntity.ok().build();

    }
}
