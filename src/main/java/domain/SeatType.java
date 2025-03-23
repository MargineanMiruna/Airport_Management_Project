package domain;

public enum SeatType {
    economy(8.0), premium_economy(12.0), business(17.0), first(21.0);

    private Double price;

    SeatType(Double price) {
        this.price = price;
    }

    public Double getPrice() {
        return price;
    }
}
