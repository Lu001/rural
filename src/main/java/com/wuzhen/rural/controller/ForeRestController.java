package com.wuzhen.rural.controller;


import com.wuzhen.rural.domain.WebSocketServer;
import com.wuzhen.rural.pojo.*;
import com.wuzhen.rural.service.*;
import com.wuzhen.rural.util.ImageUtil;
import com.wuzhen.rural.util.Page4Navigator;
import com.wuzhen.rural.util.Result;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ForeRestController {
    @Autowired
    UserService userService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    RecordService recordService;
    @Autowired
    QuestionService questionService;
    @Autowired
    QuestionRecordService questionRecordService;
    @Autowired
    OrderService orderService;
    @Autowired
    TripService tripService;
    @Autowired
    BarrageService barrageService;
    //登录注册
    @PostMapping("/login")
    public Object login(@RequestBody User user, HttpSession session){

        User user1=userService.get(user.getName(),user.getPassword());
        if (user1==null){
            String message="账号密码错误";
            return Result.fail(message);
        }else {
            session.setAttribute("user", user1);
            return Result.success();
        }
    }
    @PostMapping("/register")
    public Object register(@RequestBody User user) {
        boolean exist = userService.isExit(user.getName());
        if (exist) {
            String message = "用户名已经存在，不能使用";
            return Result.fail(message);
        }
        userService.add(user);
        return Result.success();
    }
    @GetMapping("checkLogin")
    public Object checkLogin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null != user)
            return Result.success();
        return Result.fail("未登录");
    }
    //乌镇文化分页展示
    @PostMapping("/foreCategories/{cid}/foreProducts")
    public List<Product> list(@PathVariable("cid") int cid) throws Exception {
        List<Product> page = productService.list(cid);
        productImageService.setFirstProdutImages(page);
        return page;
    }
    //购物车页面展示
    @GetMapping("/front_cart_show")
    public Object front_cart_show(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");
        List<Record> records = recordService.listByUser(user);
        productImageService.setFirstProdutImagesOnOrderItems(records);
        return records;
    }
    @GetMapping("/changeNum")
    public Object changeNum(HttpSession session, @RequestParam(value = "pid") Integer pid, @RequestParam(value = "num") Integer num) {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");
        List<Record> records = recordService.listByUser(user);
        for (Record record : records) {
            if (record.getProduct().getId() == pid) {
                record.setNumber(num);
                recordService.update(record);
                break;
            }
        }
        return Result.success();
    }
    //加入购物车
    @PostMapping("/addRecord")
    public Object addRecord(@RequestParam(value = "pid") Integer  pid, @RequestParam(value = "num") Integer num, HttpSession session) {
        Product product = productService.get(pid);
        int rid = 0;
        User user = (User) session.getAttribute("user");
        boolean found = false;
        List<Record> records = recordService.listByUser(user);
        for (Record record : records) {
            if (record.getProduct().getId() == product.getId()) {
                record.setNumber(record.getNumber() + num);
                recordService.update(record);
                found = true;
                rid = record.getId();
                break;
            }
        }
        if (!found) {
            Record record = new Record();
            record.setUser(user);
            record.setProduct(product);
            record.setNumber(num);
            recordService.add(record);
            rid = record.getId();
        }
        session.setAttribute("user", user);
        userService.update(user);
        return rid;
    }
    //删除商品
    @DeleteMapping("delProduct")
    public Object delProduct(HttpSession session, @RequestParam(value = "rid") int rid) {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");
        recordService.delete(rid);
        return Result.success();
    }
    //生成订单
    @PostMapping("forecreateOrder")
    public Object createOrder(@RequestBody Order order, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
        order.setOrderCode(orderCode);
        order.setCreateDate(new Date());
        order.setUser(user);
        order.setStatus(OrderService.waitPay);

        List<Record> records = (List<Record>) session.getAttribute("records");
        System.out.println(records);
        System.out.println("_____order_____");
        //添加order
        float total = orderService.add(order, records);
        //删除已经购买的购物车里的数据
        for (Record record : records) {
            if (record.getOrder().getId() == order.getId()) {
                recordService.delete(record.getId());
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", order.getId());
        map.put("total", total);
        return Result.success(map);
    }
    //结算页面
    @GetMapping("forebuy")
    public Object forebuy(@RequestParam(value = "oid") Integer[] products, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");
        List<Record> records = new ArrayList<>();
        float total = 0;
        for (Integer id : products) {
            Record record = recordService.get(id);
            total += record.getProduct().getPrice() * record.getNumber();
            records.add(record);
        }
        productImageService.setFirstProdutImagesOnOrderItems(records);
        session.setAttribute("records", records);
        Map<String, Object> map = new HashMap<>();
        map.put("records", records);
        map.put("total", total);
        return Result.success(map);
    }
    //购买成功
    @GetMapping("forepayed")
    public Object payed(int oid) {
        Order order = orderService.get(oid);
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        orderService.update(order);
        return order;
    }
    //写游记
    @PostMapping("writeTrip")
    public Object writeTrip(@RequestParam String content, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null == user)
            return Result.fail("未登录");
        Trip trip=new Trip();
        trip.setContent(content);
        trip.setCreateDate(new Date());
        trip.setUser(user);
        tripService.add(trip);
        Map<String, Object> map = new HashMap<>();
        map.put("trip", trip);
         return Result.success(map);
    }



    //更新操作
    @PostMapping("/foreUpdate")
    public Object update(@RequestParam(value = "name") String name, @RequestParam(value = "image") MultipartFile image, HttpServletRequest request) throws IOException {
        System.out.println(image);
        User user = new User();
        user.setName(name);
        userService.update(user);
        if (image != null) {
            saveOrUpdateImageFile(user, image, request);
        }
        return user;
    }

    private void saveOrUpdateImageFile(User user, MultipartFile image, HttpServletRequest request) throws IOException {
        File imageFolder = new File(request.getServletContext().getRealPath("img/user"));
        File file = new File(imageFolder, user.getId() + ".jpg");
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        image.transferTo(file);
        BufferedImage img = ImageUtil.change2jpg(file);
        ImageIO.write(img, "jpg", file);
    }

    //用户的评论，民俗以及购买过的商品展示
    @GetMapping("/userProducts")
    public Object userProducts(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null != user) {
            List<Record> records = recordService.list(user);
            List<Review> reviews = reviewService.list(user);
            List<Question> questions = questionService.list();
            Map<String, Object> map = new HashMap<>();
            map.put("questions", questions);
            map.put("records", records);
            map.put("reviews", reviews);
            map.put("user", user);
            return Result.success(map);
        }
        return Result.fail("未登录");
    }
    @GetMapping("/foreProducts/{pid}")
    public Object product(@PathVariable("pid") int pid, HttpSession session) {
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
        System.out.println("--------" + products);
        List<PropertyValue> pvs = propertyValueService.list(product);
        List<Review> reviews = reviewService.list(product);
        //productService.setSaleAndReviewNumber(product);
        productImageService.setFirstProdutImage(product);
        Map<String, Object> map = new HashMap<>();

        map.put("products", products);
        map.put("product", product);
        map.put("pvs", pvs);
        map.put("reviews", reviews);
        map.put("user", user);
        return Result.success(map);
    }
    //用户评价
    @PostMapping("foredoreview")
    public Object doreview(HttpSession session, int pid, String content) {
        Product p = productService.get(pid);
        content = HtmlUtils.htmlEscape(content);
        User user = (User) session.getAttribute("user");
        Review review = new Review();
        review.setContent(content);
        review.setProduct(p);
        //review.setCreateDate(new Date());
        review.setUser(user);
        reviewService.add(review);
        return Result.success();
    }
    //首页交互(购买)
    @PostMapping("/foreBuyOne")
    public Object buyOne(int pid, HttpSession session) {
        Product product = productService.get(pid);
        User user = (User) session.getAttribute("user");
        Record record = new Record(product, user);
        float userPoints = user.getPoints() - product.getPrice();
        user.setPoints(userPoints);
        session.setAttribute("user", user);
        userService.update(user);
        recordService.add(record);
        return Result.success();
    }
    @GetMapping("forebuyone")
    public Object buyone(int pid, int num, HttpSession session) {
        return addRecord(pid, num, session);
    }
    //添加问卷记录
    @PostMapping("/addQuestionRecord")
    public Object addQuestionRecord(HttpSession session, @RequestBody Question_Record qr) {
        User user = (User) session.getAttribute("user");
        Question_Record questionRecord = new Question_Record();
        questionRecord.setAcnumber(qr.getAcnumber());
        questionRecord.setErunmber(qr.getErunmber());
        float points = user.getPoints() + qr.getAcnumber() * 10;
        user.setPoints(points);
        userService.update(user);
        questionRecord.setUser(user);
        questionRecordService.add(questionRecord);
        return Result.success();
    }

    //弹幕视频展示 以及 发送
    @GetMapping("showBarrage")
    public Object showBarrage(HttpSession session) {
        return barrageService.list();
    }

    @PostMapping("sendBarrage")
    public Object sendBarrage(HttpSession session, @RequestParam(value = "barrage") String barrage) {
        Barrage b = new Barrage();
        b.setDanmu(barrage);
        System.out.println(barrage);
        try {
            WebSocketServer.BroadCastInfo(barrage);
            barrageService.add(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.success();
    }

}


/*发表文章,显示文章*/
/*
@PostMapping("/addTrap")
public Object addTrap(HttpSession session,String content) {

    User user =(User)  session.getAttribute("user");
    Trap  trap = new Trap();
    trap.setContent(content);
    //review.setCreateDate(new Date());
    trap.setUser(user);
    trapService.add(trap);
    return Result.success();
}
    @GetMapping("showAllTrap")
    public Object showAllTrap(){
        return trapService.list();
    }
    @GetMapping("showMyTrap")
    public Object showTrap(HttpSession session){
        User user =(User)  session.getAttribute("user");
    return trapService.list(user);
    }
*/
