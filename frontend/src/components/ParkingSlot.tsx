import React, { useState, useEffect } from "react";
import { ParkingSlotType } from "../types/Parking";
import "../App.css";

interface Props {
  slot: ParkingSlotType;
}

const ParkingSlotComponent: React.FC<Props> = ({ slot }) => {
  const isOccupied = slot.status === "OCCUPIED";

  const [durationText, setDurationText] = useState<string>("0m");
  const [feeText, setFeeText] = useState<string>("€0.00");
  const [feeStatusClass, setFeeStatusClass] = useState<string>("grace");

  useEffect(() => {
    if (!isOccupied || !slot.entryTime) return;

    const calculateLiveValues = () => {
      const entry = new Date(slot.entryTime!);
      const now = new Date();
      const diffInMinutes = Math.max(0, Math.floor((now.getTime() - entry.getTime()) / (1000 * 60)));

      const hours = Math.floor(diffInMinutes / 60);
      const mins = diffInMinutes % 60;
      setDurationText(hours > 0 ? `${hours}h ${mins}m` : `${mins}m`);

      if (diffInMinutes <= 15) {
        setFeeText("€0.00");
        setFeeStatusClass("grace");
      } else {
        const billedHours = Math.ceil(diffInMinutes / 60.0);
        let calculatedFee = billedHours * 3.0;

        if (calculatedFee >= 25.0) {
          calculatedFee = 25.0;
          setFeeStatusClass("cap");
        } else {
          setFeeStatusClass("active");
        }
        setFeeText(`€${calculatedFee.toFixed(2)}`);
      }
    };

    calculateLiveValues();
    const timer = setInterval(calculateLiveValues, 30000);
    return () => clearInterval(timer);
  }, [isOccupied, slot.entryTime]);

  return (
    <div className={`slot-container ${slot.status.toLowerCase()}`}>
      <div className="lane-line left"></div>
      <div className="slot-id-label">{slot.slotNumber}</div>

      {isOccupied && (
        <div className="car-top-view">
          <div className="windshield-front"></div>
          <div className="windshield-rear"></div>

          <div className="car-metrics-container">
            <span className="live-duration">{durationText}</span>
            <span className={`live-fee ${feeStatusClass}`}>{feeText}</span>
            {slot.carNumber && (
              <div className="german-plate">{slot.carNumber}</div>
            )}
          </div>
        </div>
      )}

      <div className="lane-line right"></div>
    </div>
  );
};

export default ParkingSlotComponent;