package com.clone.kurly.service;

import com.clone.kurly.domain.*;
import com.clone.kurly.dto.CartItemRequestDto;
import com.clone.kurly.repository.CartItemRepository;
import com.clone.kurly.repository.CartRepository;
import com.clone.kurly.repository.ProductRepository;
import com.clone.kurly.repository.UserRepository;
import com.clone.kurly.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartItemService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;


    @Autowired
    public CartItemService(UserRepository userRepository,
                           ProductRepository productRepository,
                           CartRepository cartRepository,
                           CartItemRepository cartItemRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }



//    // 카트 생성
//    public Long createCart(Long uid) {
//
//        User user = userRepository.findById(uid).orElseThrow(
//                ()-> new NullPointerException("유저가 존재하지 않습니다."));
//
//        Cart cart = new Cart (user);
//        cartRepository.save(cart);
//        System.out.println("유저 카트 생성");
//
//        Long cid = cart.getCid();
//
//        return cid;
//    }

    // 카트 조회 (장바구니 조회)
    public List<CartItemRequestDto> showCart(Long uid) {

        Cart cart = cartRepository.findByUser_Id(uid).orElseThrow(
                ()-> new NullPointerException("카트가 존재하지 않습니다."));

        //카트 조회 함수
        return getCartItemRequestDtoList(uid, cart);

    }


    // 카트 아이템 생성 (장바구니 등록)
    @Transactional
    public List<CartItemRequestDto> createCartItem(CartItemRequestDto cartItemRequestDto) {

        Long uid = cartItemRequestDto.getUid();
        Long pid = cartItemRequestDto.getPid();
        Long quantity = cartItemRequestDto.getQuantity();


        User user = userRepository.findById(uid).orElseThrow(
                ()-> new NullPointerException("유저가 존재하지 않습니다."));


        Product product = productRepository.findByPid(pid);

        Cart cart = cartRepository.findByUser_Id(uid).orElseThrow(
                ()-> new NullPointerException("카트가 존재하지 않습니다."));

        //카트에 이미 동일상품 중복되었는지 확인
        if(cartItemRepository.findByUser_IdAndProduct_Pid(uid,pid).isPresent()) {
            System.out.println("카트에 동일상품 확인");
            CartItem cartItem = cartItemRepository.findByUser_IdAndProduct_Pid(uid,pid).orElseThrow(
                    ()-> new NullPointerException("카트 아이템이 존재하지 않습니다."));
            System.out.println("증가할 quantity : " + quantity);
            System.out.println("원래 quantity : " + cartItem.getQuantity() );
            quantity = quantity + cartItem.getQuantity();


            cartItem.update(quantity);
            System.out.println("카트아이템 중복 등록으로 인한 수량 증가 완료");

            //카트 조회 함수
            return getCartItemRequestDtoList(uid, cart);

        } else {

            CartItem cartItem = new CartItem(cartItemRequestDto, user, product, cart);

            cartItemRepository.save(cartItem);
            System.out.println("카트아이템 생성완료");


            //등록 후 바로 카트에 담긴 전 품목 리턴함(프론트 요청)
            //카트 조회 함수
            return getCartItemRequestDtoList(uid, cart);
        }

    }

    //중복 코드로인한 추출 함수 , 카트 조회 함수
    private List<CartItemRequestDto> getCartItemRequestDtoList(Long uid, Cart cart) {
        Long cid2 = cart.getCid();
        List<CartItem>cartItemList = cartItemRepository.findAllByCart_Cid(cid2);

        List<CartItemRequestDto> cartItemRequestDtoList = new ArrayList<>();
        for (CartItem cartItem2 : cartItemList) {
            Long pid2 = cartItem2.getProduct().getPid();
            Product product2 = productRepository.findByPid(pid2);

            String title = product2.getName();
            Long price =product2.getDiscountedPrice();
            String img =product2.getDetailImageUrl();
            Long quantity =cartItem2.getQuantity();
            Long cartItemId2 =cartItem2.getCartItemId();

            CartItemRequestDto cartItemRequestDto2 = new CartItemRequestDto();
            cartItemRequestDto2.setUid(uid);
            cartItemRequestDto2.setPid(pid2);
            cartItemRequestDto2.setTitle(title);
            cartItemRequestDto2.setPrice(price);
            cartItemRequestDto2.setImg(img);
            cartItemRequestDto2.setQuantity(quantity);
            cartItemRequestDto2.setCartItemId(cartItemId2);

            cartItemRequestDtoList.add(cartItemRequestDto2);
        }

        return cartItemRequestDtoList;
    }

    // 카트 아이템 삭제
    public void deleteCartItem(Long pid, UserDetailsImpl userDetails){
        Long uid = userDetails.getId();
        CartItem cartItem = cartItemRepository.findByUser_IdAndProduct_Pid(uid,pid).orElseThrow(
                ()-> new NullPointerException("카트 아이템이 존재하지 않습니다."));
        Long cartItemId = cartItem.getCartItemId();

        cartItemRepository.deleteById(cartItemId);
        System.out.println("카트 아이템 삭제 카트아이템 id : " + cartItemId);
    }

    // 카트 아이템 수량 수정
    @Transactional
    public void updateCartItem(CartItemRequestDto cartItemRequestDto, UserDetailsImpl userDetails) {
        Long uid = userDetails.getId();
        System.out.println("uid : " + uid );
        Long pid = cartItemRequestDto.getPid();
        System.out.println("pid : " + pid );

        Long quantity = cartItemRequestDto.getQuantity();
        System.out.println("quantity : " +quantity);

        CartItem cartItem = cartItemRepository.findByUser_IdAndProduct_Pid(uid,pid).orElseThrow(
                ()-> new NullPointerException("카트 아이템이 존재하지 않습니다."));

        cartItem.update(quantity);
        System.out.println("수량 수정 완료");
    }

}