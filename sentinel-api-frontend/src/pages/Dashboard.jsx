import { useEffect, useState } from "react";

import MainLayout from "../layouts/MainLayout";
import StatCard from "../components/DashComponents/StatCard";
import HealthCard from "../components/DashComponents/HealthCard";
import AlertsCard from "../components/DashComponents/AlertsCard";
import ChartCard from "../components/DashComponents/ChartCard";
import SlowApiTable from "../components/DashComponents/SlowApiTable";

import {
    getDashboard,
    getApiHealth,
    getRecentMetrics,
} from "../services/dashboardService";

function Dashboard() {

    const [dashboard, setDashboard] = useState(null);
    const [health, setHealth] = useState([]);
    const [metrics, setMetrics] = useState([]);

    useEffect(() => {
        loadDashboard();
        const interval = setInterval(() => {
                loadDashboard();
            }, 30000);
            return () => clearInterval(interval);
    }, []);

    const loadDashboard = async () => {
        try {
            const dashboardData = await getDashboard();
            const healthData = await getApiHealth();
            const metricsData = await getRecentMetrics();

            setDashboard(dashboardData);
            setHealth(healthData);
            setMetrics(metricsData);
        } catch (error) {
            console.error("Error loading dashboard:", error);
        }
    };

    if (!dashboard) {
        return (
            <MainLayout>
                <div className="flex items-center justify-center h-96">
                    <h2 className="text-xl font-semibold text-slate-600">
                        Loading Dashboard...
                    </h2>
                </div>
            </MainLayout>
        );
    }

    return (
        <MainLayout>
            <div className="space-y-8">

                {/* Header */}
                <div>
                    <h1 className="
                        text-3xl
                        font-bold
                        text-slate-900
                        dark:text-white
                    ">
                        🟢 All Systems Operational
                    </h1>

                    <p className="
                        text-slate-500
                        dark:text-slate-400
                        mt-2
                    ">
                        Last Health Check:{" "}
                        {dashboard.lastHealthCheck
                            ? new Date(dashboard.lastHealthCheck).toLocaleString()
                            : "N/A"}
                    </p>
                </div>

                {/* Statistics */}
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">

                    <StatCard
                        title="Total APIs"
                        value="12"
                        icon="🌐"
                        type="blue"
                    />

                    <StatCard
                        title="Healthy APIs"
                        value="10"
                        icon="✓"
                        type="green"
                    />

                    <StatCard
                        title="Failed APIs"
                        value="2"
                        icon="⚠"
                        type="red"
                    />

                    <StatCard
                        title="Avg Response Time"
                        value="245 ms"
                        icon="⚡"
                        type="yellow"
                    />

                </div>

                {/* Health & Alerts */}
                <div className="
                    grid grid-cols-1 xl:grid-cols-2
                    gap-6
                ">

                    <HealthCard
                        healthy={dashboard.healthy}
                        down={dashboard.down}
                        unknown={dashboard.unknown}
                    />


                    <AlertsCard
                        alerts={dashboard.alerts || []}
                    />

                </div>

                {/* Performance Chart */}
                <ChartCard metrics={metrics} />

                {/* Slow APIs */}
                <SlowApiTable metrics={metrics} />

            </div>
        </MainLayout>
    );
}

export default Dashboard;