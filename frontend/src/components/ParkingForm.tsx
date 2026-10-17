import React, { useState } from "react";
import { enterParking } from "../services/api";

interface Props {
  onSuccess: () => void;
}

const ParkingForm: React.FC<Props> = ({ onSuccess }) => {
  const [carNumber, setCarNumber] = useState("");
  const [bulkInput, setBulkInput] = useState("");
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

  const handleBulkSubmit = async () => {
    if (!bulkInput.trim()) return;
    const carNumbers = bulkInput.split(",").map((s) => s.trim()).filter(Boolean);

    try {
      for (const plate of carNumbers) {
        await enterParking(plate);
      }
      setBulkInput("");
      setError("");
      onSuccess();
    } catch {
      setError("Failed to process bulk entry");
    }
  };

  return (
    <div className="form">
      <div style={{ marginBottom: "15px" }}>
        <input
          value={carNumber}
          onChange={(e) => setCarNumber(e.target.value.toUpperCase())}
          placeholder="B-AB 1234"
        />
        <button onClick={handleSubmit}>Enter</button>
      </div>

      <div className="header-input-group">
        <label>Multi-Car Entry (comma separated):</label>
        <div className="input-row">
          <textarea
            value={bulkInput}
            onChange={(e) => setBulkInput(e.target.value)}
            placeholder="B-AB 1234, NE-RK 8888, M-CD 5678"
            rows={2}
          />
          <button className="enter-bulk-btn" onClick={handleBulkSubmit}>Enter (Bulk)</button>
        </div>
      </div>

      {error && <p style={{ color: "red" }}>{error}</p>}
    </div>
  );
};

export default ParkingForm;