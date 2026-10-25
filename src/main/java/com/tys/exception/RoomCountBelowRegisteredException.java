package com.tys.exception;

public class RoomCountBelowRegisteredException extends CompanyBusinessException {

    public RoomCountBelowRegisteredException(long registeredRoomCount) {
        super(
                String.format(
                        "Toplam oda sayısı, sistemde kayıtlı oda sayısından (%d) az olamaz.",
                        registeredRoomCount),
                "ROOM_COUNT_BELOW_REGISTERED");
    }
}
