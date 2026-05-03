package safo_marva_tour;

import lombok.Data;

@Data
public class BookingRequest {
    private String name;
    private String phone;
    private String selectedPackage;
    private String room;
}
