public class Balance {

    private String userId;

    private String currencyId;

    private String giftSize;

    private String type;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getGiftSize() {
        return giftSize;
    }

    public void setGiftSize(String giftSize) {
        this.giftSize = giftSize;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Balance{" +
                "userId='" + userId + '\'' +
                ", currencyId='" + currencyId + '\'' +
                ", giftSize='" + giftSize + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
