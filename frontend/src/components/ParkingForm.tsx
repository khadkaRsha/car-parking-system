import React, { useState } from "react";
import { enterParking } from "../services/api";

interface Props {
  onSuccess: () => void;
}

const ParkingForm: React.FC<Props> = ({ onSuccess }) => {
  const [carNumber, setCarNumber] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async () => {
    if (!carNumber) {
      setError("Enter car number");
      return;
    }

    try {
      await enterParking(carNumber);
      setCarNumber("");
      setError("");
      onSuccess();
    } catch {
      setError("Failed to enter parking");
    }
  };

  return (
    <div className="form">
      <input
        value={carNumber}
        onChange={(e) => setCarNumber(e.target.value.toUpperCase())}
        placeholder="B-AB 1234"
      />
      <button onClick={handleSubmit}>Enter</button>
      {error && <p style={{ color: "red" }}>{error}</p>}
    </div>
  );
};

export default ParkingForm;
