export type ParkingStatus = "FREE" | "IN_PROCESS" | "OCCUPIED";

export interface ParkingSlotType {
  id: number;
  slotNumber: number;
  status: ParkingStatus;
  carNumber?: string;
}

export interface ParkingStats {
  total: number;
  occupied: number;
  queue: number;
}
