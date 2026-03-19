import React, { useEffect, useState } from "react";
import ParkingGrid from "./components/ParkingGrid";
import ParkingForm from "./components/ParkingForm";
import { ParkingSlotType, ParkingStats } from "./types/Parking";
import { getInitialData } from "./services/api";
import { connectWebSocket, disconnectWebSocket } from "./services/websocket";
import "./App.css";

const App: React.FC = () => {
  const [slots, setSlots] = useState<ParkingSlotType[]>([]);
  const [stats, setStats] = useState<ParkingStats>({
    total: 0,
    occupied: 0,
    queue: 0,
  });

  const fetchData = async () => {
    try {
      const data = await getInitialData();
      if (Array.isArray(data)) {
        const occupiedCount = data.filter((s: ParkingSlotType) => s.status === "OCCUPIED").length;
        setSlots(data);
        setStats({ total: data.length, occupied: occupiedCount, queue: 0 });
      } else if (data && Array.isArray(data.slots) && data.stats) {
        setSlots(data.slots);
        setStats({
          total: data.stats.total ?? data.slots.length,
          occupied: data.stats.occupied ?? 0,
          queue: data.stats.queue ?? 0,
        });
      }
    } catch (err) {
      console.error("Error fetching parking data", err);
    }
  };

  useEffect(() => {
    fetchData();
    connectWebSocket((message: any) => {
      if (message && Array.isArray(message.slots)) {
        setSlots(message.slots);
        setStats(message.stats);
      }
    });

    const interval = setInterval(fetchData, 5000);
    return () => { clearInterval(interval); disconnectWebSocket(); };
  }, []);

  const available = Math.max(stats.total - stats.occupied, 0);

  return (
    <div className="park-dashboard">
      {/* Professional Header Section */}
      <header className="park-header">
        <div className="header-brand">
          <span className="p-sign">P</span>
          <h1>Parkleitsystem</h1>
        </div>
        
        <div className="header-controls">
          <ParkingForm onSuccess={fetchData} />
        </div>
      </header>

      {/* Stats Dashboard */}
      <div className="stats-container">
        <div className="stat-card">
          <span className="stat-label">Verfügbar (Available)</span>
          <span className={`stat-value ${available < 5 ? 'warning' : 'success'}`}>
            {available} / {stats.total}
          </span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Warteschlange (Queue)</span>
          <span className="stat-value">{stats.queue}</span>
        </div>
      </div>

      {/* Dynamic Status Alerts */}
      <div className="status-alerts">
        {available === 1 && (
          <div className="alert alert-warning">⚠️ Nur noch 1 Stellplatz frei!</div>
        )}
        {available === 0 && (
          <div className="alert alert-danger">🚫 Parkhaus Belegt (Full)</div>
        )}
      </div>

      <ParkingGrid slots={slots} />
    </div>
  );
};

export default App;