import { ParkingSlotType, ParkingStats } from "../types/Parking";

const BASE_URL = "/api/parking";

/**
 * Fetch all parking slots from backend.
 * Handles both:
 * - Array of ParkingSlotType
 * - Object { slots: ParkingSlotType[], stats: ParkingStats }
 * Gracefully logs invalid responses.
 */
export const getInitialData = async (): Promise<
  ParkingSlotType[] | { slots: ParkingSlotType[]; stats: ParkingStats }
> => {
  try {
    const res = await fetch(`${BASE_URL}/slots`);
    const text = await res.text();

    try {
      const data = JSON.parse(text);
      return data;
    } catch (err) {
      console.error("getInitialData: Failed to parse JSON. Response was:", text);
      throw err;
    }
  } catch (err) {
    console.error("getInitialData: Fetch error:", err);
    throw err;
  }
};

/**
 * Enter a car into the parking.
 * Gracefully handles non-JSON or HTML responses.
 */
export const enterParking = async (carNumber: string) => {
  try {
    const res = await fetch(`${BASE_URL}/enter`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ carNumber }),
    });

    const text = await res.text();
    try {
      return JSON.parse(text);
    } catch {
      console.warn("enterParking: Non-JSON response:", text);
      if (!res.ok) throw new Error("Failed to enter parking");
      return text;
    }
  } catch (err) {
    console.error("enterParking: Fetch error:", err);
    throw err;
  }
};