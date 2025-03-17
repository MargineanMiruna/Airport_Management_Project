package domain;

public enum Bag {
    small(5, 0.0), cabin(10, 10.0), check_in(20, 25.0);

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
