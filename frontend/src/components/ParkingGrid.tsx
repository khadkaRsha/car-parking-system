import React, { useState, useEffect } from "react";
import { ParkingSlotType } from "../types/Parking";
import { getInitialData, enterParking, searchParkingHistory, exitParking } from "../services/api";
import '../App.css';

interface Props {
  slots?: ParkingSlotType[];
  onRefresh?: () => void;
}

interface ParkingHistoryItem {
  id: number;
  entryTime: string;
  exitTime?: string;
  car?: { carNumber: string };
  carNumber?: string;
  duration?: string;
  fee?: number | string;
  status?: string;
}

export default function ParkingGrid({ slots: propSlots, onRefresh }: Props) {
  const [slots, setSlots] = useState<ParkingSlotType[]>(propSlots || []);
  const [loading, setLoading] = useState<boolean>(!propSlots);
  const [activeSlot, setActiveSlot] = useState<ParkingSlotType | null>(null);
  const [inputValue, setInputValue] = useState("");
  const [searchPlate, setSearchPlate] = useState("");
  const [filteredHistory, setFilteredHistory] = useState<ParkingHistoryItem[]>([]);

  const fetchSlotsData = async () => {
    try {
      setLoading(true);
      const data = await getInitialData();
      if (Array.isArray(data)) {
        setSlots(data);
      } else if (data && data.slots) {
        setSlots(data.slots);
      }
    } catch (err) {
      console.error("Failed to load parking slots from backend", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSlotsData();
    const interval = setInterval(fetchSlotsData, 10000);
    return () => clearInterval(interval);
  }, []);

  const handleSmartSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputValue.trim()) return;

    const carNumbers = inputValue.split(",").map((s) => s.trim()).filter(Boolean);

    try {
      for (const plate of carNumbers) {
        await enterParking(plate);
      }
      setInputValue("");
      await fetchSlotsData();
      if (onRefresh) onRefresh();
    } catch (err) {
      alert("Failed to process entry");
    }
  };

  // Basic Exit / Remove Operation with instant grid re-fetch
  const handleExitCar = async (slotId: number, carNumber?: string) => {
    const vehicleText = carNumber ? `vehicle (${carNumber})` : "this vehicle";
    const isConfirmed = window.confirm(
      `Are you sure you want to check out ${vehicleText}?.`
    );

    // 2. If user clicks "Cancel", stop execution here
    if (!isConfirmed) {
      return;
    }

    try {
      await exitParking(slotId);
      setActiveSlot(null);
      
      // Force immediate re-fetch from backend
      await fetchSlotsData();
      if (onRefresh) onRefresh();
    } catch (err: any) {
      console.error("Exit error:", err);
      alert(err.message || "Failed to process exit");
    }
  };
  

  const handleSearchHistory = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchPlate.trim()) {
      setFilteredHistory([]);
      return;
    }

    try {
      const results = await searchParkingHistory(searchPlate.trim());
      setFilteredHistory(Array.isArray(results) ? results : [results]);
    } catch (err) {
      console.error("Error searching parking history:", err);
      setFilteredHistory([]);
    }
  };

  const scrollToHistory = () => {
    const historyElement = document.getElementById("parking-history-section");
    if (historyElement) {
      historyElement.scrollIntoView({ behavior: "smooth" });
    }
  };

  const calculateDuration = (entryTime?: string, exitTime?: string) => {
    if (!entryTime) return "0m";
    const entry = new Date(entryTime);
    const exit = exitTime ? new Date(exitTime) : new Date();
    const diffInMinutes = Math.max(0, Math.floor((exit.getTime() - entry.getTime()) / (1000 * 60)));
    const hours = Math.floor(diffInMinutes / 60);
    const mins = diffInMinutes % 60;
    return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
  };

  const formatParkedSince = (entryTime?: string) => {
    if (!entryTime) return "--:--";
    const entry = new Date(entryTime);
    return entry.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const totalSlots = slots.length > 0 ? slots.length : 50;
  const occupiedCount = slots.filter(s => s.status === 'OCCUPIED').length;
  const availableCount = totalSlots - occupiedCount;

  return (
    <div className="parkleit-container">
      {/* Header Dashboard Bar */}
      <header className="dashboard-header">
        <div className="header-title">
          <span className="p-badge">P</span>
          <h1>Parkleitsystem</h1>
        </div>

        <div className="header-right-controls" style={{ display: 'flex', gap: '20px' }}>
          {/* Multi-Car Entry */}
          <div className="header-input-group">
            <label>MULTI-CAR ENTRY (COMMA SEPARATED):</label>
            <form onSubmit={handleSmartSubmit} className="input-row">
              <textarea 
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value.toUpperCase())}
                placeholder="B-AB 1234, NE-RK 8888" 
                rows={1}
              />
              <button type="submit" className="enter-bulk-btn">Enter (Bulk)</button>
            </form>
          </div>

          {/* Search History Box */}
          <div className="header-input-group">
            <label>SEARCH HISTORY (LICENSE PLATE):</label>
            <form onSubmit={handleSearchHistory} className="input-row">
              <input 
                type="text"
                value={searchPlate}
                onChange={(e) => setSearchPlate(e.target.value.toUpperCase())}
                placeholder="B-AB 1234"
              />
              <button type="submit" className="enter-bulk-btn">SEARCH</button>
            </form>
          </div>
        </div>
      </header>

      {/* Metrics Banner */}
      <div className="metrics-banner">
        <div className="metric-card">
          <span className="metric-label">VERFÜGBAR (AVAILABLE)</span>
          <span className="metric-value available-text">{availableCount} / {totalSlots}</span>
        </div>
        <div className="metric-card">
          <span className="metric-label">BESETZT (OCCUPIED)</span>
          <span className="metric-value">{occupiedCount}</span>
        </div>
        <button onClick={scrollToHistory} className="enter-bulk-btn" style={{ alignSelf: 'center', height: '42px', padding: '0 20px' }}>
          View History List
        </button>
      </div>

      {/* Parking Grid Area */}
      <div className="grid-section" style={{ position: 'relative' }}>
        {loading && <p style={{ textAlign: 'center', padding: '20px', color: '#fff' }}>Loading grid details...</p>}

        <div className="slots-grid">
          {!loading && slots.map((slot) => {
            const isOccupied = slot.status === 'OCCUPIED';
            const isHovered = activeSlot?.id === slot.id;

            return (
              <div 
                key={slot.id} 
                className="slot-card" 
                style={{ position: 'relative' }}
                onMouseEnter={() => {
                  if (isOccupied) setActiveSlot(slot);
                }}
                onMouseLeave={() => {
                  setActiveSlot(null);
                }}
              >
                <span className="slot-number">{slot.slotNumber || slot.id}</span>
                
                {isOccupied ? (
                  <>
                    <div className="car-top-view">
                      <div className="car-body">
                        <div className="car-sensor-dot"></div>
                      </div>
                      <div className="car-metrics-container">
                        <span className="live-duration">{calculateDuration(slot.entryTime)}</span>
                        <span className="live-fee">€{slot.fee?.toFixed(2) || '0.00'}</span>
                        {slot.carNumber && (
                          <span className="car-plate-badge">{slot.carNumber}</span>
                        )}
                      </div>
                    </div>

                    {/* Controlled Popover - Displays only for the single hovered slot */}
                    {isHovered && (
                      <div 
                        className="slot-hover-popover" 
                        onMouseEnter={() => setActiveSlot(slot)}
                        onMouseLeave={() => setActiveSlot(null)}
                        style={{
                          position: 'absolute',
                          bottom: '100%',
                          left: '50%',
                          transform: 'translateX(-50%) translateY(-2px)',
                          background: '#0f172a',
                          border: '1px solid #334155',
                          padding: '12px',
                          borderRadius: '8px',
                          zIndex: 100,
                          width: '180px',
                          boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.5)',
                          color: '#fff',
                          textAlign: 'left'
                        }}
                      >
                        <div className="popover-header" style={{ marginBottom: '8px', borderBottom: '1px solid #334155', paddingBottom: '4px' }}>
                          <strong>Slot {slot.slotNumber || slot.id}</strong>
                        </div>
                        <div className="popover-body" style={{ fontSize: '0.85rem', marginBottom: '8px' }}>
                          <p style={{ margin: '2px 0' }}><strong>Plate:</strong> {slot.carNumber || 'N/A'}</p>
                          <p style={{ margin: '2px 0' }}><strong>Since:</strong> {formatParkedSince(slot.entryTime)}</p>
                          <p style={{ margin: '2px 0' }}><strong>Duration:</strong> {calculateDuration(slot.entryTime)}</p>
                        </div>
                        <button 
                          type="button"
                          className="exit-pay-btn" 
                          onClick={(e) => {
                            e.stopPropagation();
                            handleExitCar(slot.id, slot.carNumber);
                          }}
                          style={{ width: '100%', padding: '6px', background: '#dc2626', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                        >
                          Remove Car (Exit)
                        </button>
                      </div>
                    )}
                  </>
                ) : (
                  <div className="empty-slot-indicator"></div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Legend Footer */}
      <footer className="grid-legend">
        <span className="legend-item"><strong className="green-text">Green</strong> = Grace Period (€0.00)</span>
        <span className="legend-item"><strong className="amber-text">Amber</strong> = Active Charging</span>
        <span className="legend-item"><strong className="red-text">Red</strong> = Near Daily Cap (€25.00)</span>
      </footer>

      {/* Bottom Section: Parking History List */}
      <section id="parking-history-section" className="history-section" style={{ marginTop: '32px', background: '#1e293b', padding: '24px', borderRadius: '16px', color: '#fff' }}>
        <div className="history-header" style={{ marginBottom: '16px' }}>
          <h3 style={{ margin: 0 }}>PARKHISTORIE (PARKING HISTORY LIST)</h3>
        </div>
        <div className="history-table-container">
          <table className="history-table" style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #334155', color: '#94a3b8' }}>
                <th style={{ padding: '10px' }}>Datum/Zeit (Date/Time)</th>
                <th style={{ padding: '10px' }}>Autokennzeichen (License Plate)</th>
                <th style={{ padding: '10px' }}>Dauer (Total Duration)</th>
                <th style={{ padding: '10px' }}>Gesamtgebühr (Final Fee)</th>
                <th style={{ padding: '10px' }}>Status</th>
              </tr>
            </thead>
            <tbody>
              {filteredHistory.length > 0 ? (
                filteredHistory.map((item) => {
                  const plate = item.car?.carNumber || item.carNumber || 'N/A';
                  const entryFormatted = item.entryTime ? new Date(item.entryTime).toLocaleString() : '-';
                  const durationStr = item.duration || calculateDuration(item.entryTime, item.exitTime);
                  const feeVal = typeof item.fee === 'number' ? `€${item.fee.toFixed(2)}` : (item.fee || '€0.00');
                  const isClosed = Boolean(item.exitTime);
                  const statusLabel = item.status || (isClosed ? "PAID" : "ACTIVE");

                  return (
                    <tr key={item.id} style={{ borderBottom: '1px solid #334155' }}>
                      <td style={{ padding: '12px 10px' }}>{entryFormatted}</td>
                      <td style={{ padding: '12px 10px' }}><strong>{plate}</strong></td>
                      <td style={{ padding: '12px 10px' }}>{durationStr}</td>
                      <td style={{ padding: '12px 10px' }}>{feeVal}</td>
                      <td style={{ padding: '12px 10px' }}>
                        <span className={isClosed ? "status-paid" : "status-active"} style={{ padding: '4px 8px', borderRadius: '4px', background: isClosed ? '#166534' : '#854d0e', fontSize: '0.8rem' }}>
                          {statusLabel}
                        </span>
                      </td>
                    </tr>
                  );
                })
              ) : (
                <tr>
                  <td colSpan={5} style={{ textAlign: 'center', padding: '20px', color: '#94a3b8' }}>
                    {searchPlate ? `No history records found for plate "${searchPlate}"` : "Enter a license plate above to search parking history."}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}