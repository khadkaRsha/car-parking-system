import React from "react";
import ParkingSlot from "./ParkingSlot";
import { ParkingSlotType } from "../types/Parking";

interface Props {
  slots: ParkingSlotType[];
}

const ParkingGrid: React.FC<Props> = ({ slots }) => {
  return (
    <div className="garage-floor">
      <div className="parking-grid">
        {slots.map((slot) => (
          <ParkingSlot key={slot.id} slot={slot} />
        ))}
      </div>
    </div>
  );
};

export default ParkingGrid;