package com.wuzhen.rural.controller;



import com.wuzhen.rural.pojo.*;
import com.wuzhen.rural.service.*;
import com.wuzhen.rural.util.Page4Navigator;
import com.wuzhen.rural.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.util.List;


@Controller
public class ForePageController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    QuestionService questionService;
    @Autowired
    QuestionRecordService questionRecordService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    RecordService recordService;
    @Autowired
    UserService userService;
    @Autowired
    TripService tripService;
    //乌镇首页 乡土 文化 特产 风光

    @GetMapping(value = "/frontIndex")
    public String frontIndex() {
        return "rural/main";
    }
    @GetMapping(value = "/home")
    public String home(Model m) {
        List<Product> products=productService.list(3);
        productImageService.setFirstProdutImages(products);
        m.addAttribute("ps",products);
        return "rural/home";
    }


    //退出登录
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:home";
    }

    //登录注册
    @GetMapping(value = "/rural_login")
    public String login() {
        return "rural/fore/login";
    }

    @GetMapping(value = "/register")
    public String register() {
        return "rural/fore/register";
    }

    //评论
    @GetMapping(value = "/review")
    public String review() {
        return "rural/fore/review";
    }



    //展示风景
   /*GetMapping(value="/showSign")
    public String showSign(Model m){
        List<Category> categories=categoryService.list();
        List<Product> products=productService.list();
        productService.fill(categories);
        m.addAttribute("cs",categories);
        m.addAttribute("ps",products);
        return "rural/front/sign";
    }*/
    //购物车 结算 订单
    @GetMapping(value = "/front_cart_list")
    public String front_cart_list() {
        return "rural/cart";
    }

    @GetMapping(value = "/front_check_list")
    public String front_check_list() {
        return "rural/checkout";
    }

    @GetMapping(value = "/front_order_list")
    public String front_order_list() {
        return "rural/order";
    }

    //乌镇首页 乌镇乡土 乌镇文化 乌镇游记 乌镇特产的展示
    @GetMapping(value = "/front_land_list")
    public String front_land_list(Model m, @RequestParam(value = "cid") int cid){
        Category categories=categoryService.get(cid);
        List<Product> products=productService.list(cid);
        productImageService.setFirstProdutImages(products);
        m.addAttribute("cs",categories);
        m.addAttribute("ps",products);
        return "rural/land";
    }
    @GetMapping(value = "/front_folk_list")
    public String front_folk_list() {
        return "rural/folk";
    }
    @GetMapping(value = "/front_food_list")
    public String front_food_list() {
        return "rural/food";
    }
    //乌镇特产详情
    @GetMapping(value = "/front_foodDetail_list")
    public String front_foodDetail_list(Model m, @RequestParam(value = "pid") int pid,HttpSession session){
        User user = null;
        try {
            user = (User) session.getAttribute("user");
        } catch (Exception e) {

        }
        Product product = productService.get(pid);
        List<ProductImage> productSingleImages = productImageService.listSingleProductImages(product);
        List<ProductImage> productDetailImages = productImageService.listDetailProductImages(product);
        product.setProductSingleImages(productSingleImages);
        product.setProductDetailImages(productDetailImages);
        List<Product> products = productService.list(product.getCategory().getId());
        List<PropertyValue> pvs = propertyValueService.list(product);
        List<Review> reviews = reviewService.list(product);
        //productService.setSaleAndReviewNumber(product);
        productImageService.setFirstProdutImage(product);
        for (Product p :
                products) {
            productImageService.setFirstProdutImage(p);
        }
        m.addAttribute("products",products);
        m.addAttribute("product",product);
        m.addAttribute("pvs",pvs);
        m.addAttribute("reviews",reviews);
        m.addAttribute("user",user);
        return "rural/foodDetail";
    }
    //乌镇旅游
    @GetMapping(value = "/front_trip_list")
    public String front_trip_list(Model m){
        List<Trip> trips=tripService.list();
        m.addAttribute("trips",trips);
        return "rural/trip";
    }

    @GetMapping(value = "/front_tripDetail_list")
    public String front_tripDetail_list(Model m, @RequestParam(value = "id") int id){
        Trip trip = tripService.get(id);
        m.addAttribute("trip",trip);
        return "rural/tripDetail";
    }
    //乌镇乡土详情-->1用户评论2商品属性
    @GetMapping(value = "/front_landDetail_list")
    public String front_landDetail_list(Model m, @RequestParam(value = "pid") int pid,HttpSession session){
        User user = null;
        try {
            user = (User) session.getAttribute("user");
        } catch (Exception e) {

        }
        Product product = productService.get(pid);
        List<ProductImage> productSingleImages = productImageService.listSingleProductImages(product);
        List<ProductImage> productDetailImages = productImageService.listDetailProductImages(product);
        product.setProductSingleImages(productSingleImages);
        product.setProductDetailImages(productDetailImages);
        List<Product> products = productService.list(product.getCategory().getId());
        List<PropertyValue> pvs = propertyValueService.list(product);
        List<Review> reviews = reviewService.list(product);
        //productService.setSaleAndReviewNumber(product);
        productImageService.setFirstProdutImage(product);
        System.out.println(products+"-------");
        System.out.println(product+"-------");
        System.out.println(pvs+"-------");
        System.out.println(reviews+"-------");
        System.out.println(user+"-------");
        m.addAttribute("products",products);
        m.addAttribute("product",product);
        m.addAttribute("pvs",pvs);
        m.addAttribute("reviews",reviews);
        m.addAttribute("user",user);
        return "rural/landDetail";
    }
    //个人中心
    @GetMapping(value = "/front_user")
    public String front_user(Model m){
        return "rural/user";
    }

    //登录注册
    @PostMapping("/front_login")
    public Object front_login(@RequestParam(value = "name") String name, @RequestParam(value = "password") String password, HttpSession session) {
        name = HtmlUtils.htmlEscape(name);
        User user = userService.get(name, password);
        if (user == null) {
            String message = "账号密码错误";
            return Result.fail(message);
        } else {
            session.setAttribute("user", user);
            return Result.success();
        }
    }


}