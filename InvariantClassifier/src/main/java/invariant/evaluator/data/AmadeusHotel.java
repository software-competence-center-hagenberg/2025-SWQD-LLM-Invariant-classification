package invariant.evaluator.data;

public class AmadeusHotel extends AgoraDataSet {

    public static final String OPEN_API_SPECIFICATION = "agora_data/apiSpecifications/AmadeusHotel/swagger.yaml";

    public static final String SYSTEM_NAME = "AmadeusHotel";

    public static final String PATH = "/shopping/hotel-offers";

    public static final String OPERATION_NAME = "getMultiHotelOffers";

    public static final String RESPONSE_CODE = "200";



    public static final AgoraDataSet AMADEUS_HOTEL_OFFERS_50_ORIGINAL = new AmadeusHotel(50, true);
    public static final AgoraDataSet AMADEUS_HOTEL_OFFERS_50_MODIFIED = new AmadeusHotel(50, false);

    public static final AgoraDataSet AMADEUS_HOTEL_OFFERS_100_ORIGINAL = new AmadeusHotel(100, true);
    public static final AgoraDataSet AMADEUS_HOTEL_OFFERS_100_MODIFIED = new AmadeusHotel(100, false);
    public static final AgoraDataSet AMADEUS_HOTEL_OFFERS_500_ORIGINAL = new AmadeusHotel(500, true);
    public static final AgoraDataSet AMADEUS_HOTEL_OFFERS_500_MODIFIED = new AmadeusHotel(500, false);
    public static final AgoraDataSet AMADEUS_HOTEL_OFFERS_1000_ORIGINAL = new AmadeusHotel(1000, true);
    public static final AgoraDataSet AMADEUS_HOTEL_OFFERS_1000_MODIFIED = new AmadeusHotel(1000, false);
    public static final AgoraDataSet AMADEUS_HOTEL_OFFERS_10000_ORIGINAL = new AmadeusHotel(10000, true);
    public static final AgoraDataSet AMADEUS_HOTEL_OFFERS_10000_MODIFIED = new AmadeusHotel(10000, false);

    public static final AgoraDataSet[] ALL_DATA_SETS = new AgoraDataSet[]{
            AMADEUS_HOTEL_OFFERS_50_ORIGINAL,
            AMADEUS_HOTEL_OFFERS_50_MODIFIED,
            AMADEUS_HOTEL_OFFERS_100_ORIGINAL,
            AMADEUS_HOTEL_OFFERS_100_MODIFIED,
            AMADEUS_HOTEL_OFFERS_500_ORIGINAL,
            AMADEUS_HOTEL_OFFERS_500_MODIFIED,
            AMADEUS_HOTEL_OFFERS_1000_ORIGINAL,
            AMADEUS_HOTEL_OFFERS_1000_MODIFIED,
            AMADEUS_HOTEL_OFFERS_10000_ORIGINAL,
            AMADEUS_HOTEL_OFFERS_10000_MODIFIED
    };

    public static final MergedDataSet AMADEUS_HOTEL_OFFERS = new MergedDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet AMADEUS_HOTEL_OFFERS_UNIQUE = new UniqueInvariantAgoraDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet UNIQUE_AMADEUS_HOTEL_OFFERS = new UniqueInvariantAgoraDataSet(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE);

    public AmadeusHotel(long numberOfApiCalls, boolean original) {
        super(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE, numberOfApiCalls, original);
    }
}
