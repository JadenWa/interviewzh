import java.util.HashMap;
import java.util.Map;

// 水果基类
abstract class Fruit {
    private String name;
    private double pricePerKg;
    
    public Fruit(String name, double pricePerKg) {
        this.name = name;
        this.pricePerKg = pricePerKg;
    }
    
    public String getName() {
        return name;
    }
    
    public double getPricePerKg() {
        return pricePerKg;
    }
    
    // 计算该水果总价
    public double calculatePrice(int weight) {
        return pricePerKg * weight;
    }
}

// 具体水果类
class Apple extends Fruit {
    public Apple() {
        super("苹果", 8.0);
    }
}

class Strawberry extends Fruit {
    public Strawberry() {
        super("草莓", 13.0);
    }
}

class Mango extends Fruit {
    public Mango() {
        super("芒果", 20.0);
    }
}

// 促销策略接口
interface PromotionStrategy {
    double applyPromotion(double originalPrice, Map<Class<? extends Fruit>, Integer> items);
}

// 无促销策略
class NoPromotion implements PromotionStrategy {
    @Override
    public double applyPromotion(double originalPrice, Map<Class<? extends Fruit>, Integer> items) {
        return originalPrice;
    }
}

// 草莓打8折
class StrawberryDiscount implements PromotionStrategy {
    @Override
    public double applyPromotion(double originalPrice, Map<Class<? extends Fruit>, Integer> items) {
        // 计算草莓的原价
        double strawberryOriginalPrice = 0;
        if (items.containsKey(Strawberry.class)) {
            int strawberryWeight = items.get(Strawberry.class);
            strawberryOriginalPrice = new Strawberry().getPricePerKg() * strawberryWeight;
        }
        
        // 计算折扣后的草莓价格
        double strawberryDiscountedPrice = strawberryOriginalPrice * 0.8;
        
        // 总价 = 原总价 - 草莓原价 + 草莓折扣价
        return originalPrice - strawberryOriginalPrice + strawberryDiscountedPrice;
    }
}

// 满100减10
class Over100Discount10 implements PromotionStrategy {
    private PromotionStrategy baseStrategy;
    
    public Over100Discount10(PromotionStrategy baseStrategy) {
        this.baseStrategy = baseStrategy;
    }
    
    @Override
    public double applyPromotion(double originalPrice, Map<Class<? extends Fruit>, Integer> items) {
        double priceAfterBasePromotion = baseStrategy.applyPromotion(originalPrice, items);
        
        if (priceAfterBasePromotion >= 100) {
            return priceAfterBasePromotion - 10;
        }
        return priceAfterBasePromotion;
    }
}

// 购物车类
class ShoppingCart {
    private Map<Class<? extends Fruit>, Integer> items = new HashMap<>();
    private PromotionStrategy promotionStrategy;
    
    public void addItem(Class<? extends Fruit> fruitClass, int weight) {
        items.put(fruitClass, items.getOrDefault(fruitClass, 0) + weight);
    }
    
    public void setPromotionStrategy(PromotionStrategy promotionStrategy) {
        this.promotionStrategy = promotionStrategy;
    }
    
    public double calculateTotal() {
        double total = 0;
        
        // 计算原价
        for (Map.Entry<Class<? extends Fruit>, Integer> entry : items.entrySet()) {
            try {
                Fruit fruit = entry.getKey().newInstance();
                total += fruit.calculatePrice(entry.getValue());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        
        // 应用促销策略
        if (promotionStrategy != null) {
            total = promotionStrategy.applyPromotion(total, items);
        }
        
        return total;
    }
}

// 测试类
public class SupermarketFruits {
    
    // 顾客A：购买苹果和草莓，无促销
    public static double calculateCustomerA(int appleWeight, int strawberryWeight) {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(Apple.class, appleWeight);
        cart.addItem(Strawberry.class, strawberryWeight);
        cart.setPromotionStrategy(new NoPromotion());
        return cart.calculateTotal();
    }
    
    // 顾客B：购买苹果、草莓和芒果，无促销
    public static double calculateCustomerB(int appleWeight, int strawberryWeight, int mangoWeight) {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(Apple.class, appleWeight);
        cart.addItem(Strawberry.class, strawberryWeight);
        cart.addItem(Mango.class, mangoWeight);
        cart.setPromotionStrategy(new NoPromotion());
        return cart.calculateTotal();
    }
    
    // 顾客C：购买苹果、草莓和芒果，草莓打8折
    public static double calculateCustomerC(int appleWeight, int strawberryWeight, int mangoWeight) {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(Apple.class, appleWeight);
        cart.addItem(Strawberry.class, strawberryWeight);
        cart.addItem(Mango.class, mangoWeight);
        cart.setPromotionStrategy(new StrawberryDiscount());
        return cart.calculateTotal();
    }
    
    // 顾客D：购买苹果、草莓和芒果，草莓打8折且满100减10
    public static double calculateCustomerD(int appleWeight, int strawberryWeight, int mangoWeight) {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(Apple.class, appleWeight);
        cart.addItem(Strawberry.class, strawberryWeight);
        cart.addItem(Mango.class, mangoWeight);
        PromotionStrategy strategy = new Over100Discount10(new StrawberryDiscount());
        cart.setPromotionStrategy(strategy);
        return cart.calculateTotal();
    }
    
    // 测试验证
    public static void main(String[] args) {
        // 测试用例1：顾客A购买5斤苹果和5斤草莓
        double resultA = calculateCustomerA(5, 5);
        double expectedA = 5 * 8 + 5 * 13; // 40 + 65 = 105
        System.out.println("顾客A测试: " + (Math.abs(resultA - expectedA) < 0.01 ? "通过" : "失败"));
        System.out.println("计算结果: " + resultA + ", 预期结果: " + expectedA);
        
        // 测试用例2：顾客B购买5斤苹果、5斤草莓和5斤芒果
        double resultB = calculateCustomerB(5, 5, 5);
        double expectedB = 5 * 8 + 5 * 13 + 5 * 20; // 40 + 65 + 100 = 205
        System.out.println("顾客B测试: " + (Math.abs(resultB - expectedB) < 0.01 ? "通过" : "失败"));
        System.out.println("计算结果: " + resultB + ", 预期结果: " + expectedB);
        
        // 测试用例3：顾客C购买5斤苹果、5斤草莓和5斤芒果，草莓打8折
        double resultC = calculateCustomerC(5, 5, 5);
        double strawberryOriginal = 5 * 13; // 65
        double strawberryDiscounted = strawberryOriginal * 0.8; // 52
        double expectedC = 5 * 8 + strawberryDiscounted + 5 * 20; // 40 + 52 + 100 = 192
        System.out.println("顾客C测试: " + (Math.abs(resultC - expectedC) < 0.01 ? "通过" : "失败"));
        System.out.println("计算结果: " + resultC + ", 预期结果: " + expectedC);
        
        // 测试用例4：顾客D购买5斤苹果、5斤草莓和5斤芒果，草莓打8折且满100减10
        double resultD = calculateCustomerD(5, 5, 5);
        double expectedD = 192 - 10; // 182
        System.out.println("顾客D测试: " + (Math.abs(resultD - expectedD) < 0.01 ? "通过" : "失败"));
        System.out.println("计算结果: " + resultD + ", 预期结果: " + expectedD);
        
        // 测试用例5：顾客D购买1斤苹果、1斤草莓和1斤芒果，草莓打8折但不满足满减条件
        double resultD2 = calculateCustomerD(1, 1, 1);
        double expectedD2 = 1 * 8 + 1 * 13 * 0.8 + 1 * 20; // 8 + 10.4 + 20 = 38.4
        System.out.println("顾客D(小额)测试: " + (Math.abs(resultD2 - expectedD2) < 0.01 ? "通过" : "失败"));
        System.out.println("计算结果: " + resultD2 + ", 预期结果: " + expectedD2);
    }
}