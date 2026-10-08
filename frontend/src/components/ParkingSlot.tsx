import React from "react";
import { ParkingSlotType } from "../types/Parking";
import "../App.css";

interface Props {
  slot: ParkingSlotType;
}

const ParkingSlot: React.FC<Props> = ({ slot }) => {
  const isOccupied = slot.status === "OCCUPIED";

  return (
    <div className={`slot-container ${slot.status.toLowerCase()}`}>
      {/* Lane markings to simulate a real parking bay */}
      <div className="lane-line left"></div>
      
      <div className="slot-id-label">{slot.slotNumber}</div>

      {isOccupied && (
        <div className="car-top-view">
          <div className="windshield-front"></div>
          {slot.carNumber && (
            <div className="german-plate">
              {slot.carNumber}
            </div>
          )}
          <div className="windshield-rear"></div>
        </div>
      )}

      <div className="lane-line right"></div>
    </div>
  );
};

export default ParkingSlot;

