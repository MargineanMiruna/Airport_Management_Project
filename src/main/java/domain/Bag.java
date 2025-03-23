package domain;

public enum Bag {
    carry_on(5, 0.0), cabin_bag(10, 10.0), checked_bag(20, 25.0);

    private int maxWeight;
    private Double price;

    Bag(int maxWeight, Double price) {
        this.maxWeight = maxWeight;
        this.price = price;
    }

    public int getMaxWeight() {
        return maxWeight;
    }

    public Double getPrice() {
        return price;
    }
}
