import { useEffect, useState } from "react";

import MainLayout from "../layouts/MainLayout";
import StatCard from "../components/DashComponents/StatCard";
import HealthCard from "../components/DashComponents/HealthCard";
import AlertsCard from "../components/DashComponents/AlertsCard";
import ChartCard from "../components/DashComponents/ChartCard";
import SlowApiTable from "../components/DashComponents/SlowApiTable";

import {
    getDashboard,
    getRecentMetrics,
} from "../services/dashboardService";

function Dashboard() {

    const [dashboard, setDashboard] = useState(null);
    const [metrics, setMetrics] = useState([]);

    useEffect(() => {

        loadDashboard();

        const interval = setInterval(loadDashboard, 30000);

        return () => clearInterval(interval);

    }, []);

    const loadDashboard = async () => {

        try {

            const dashboardData = await getDashboard();
            const metricsData = await getRecentMetrics();

            setDashboard(dashboardData);
            setMetrics(metricsData);

        } catch (error) {

            console.error("Error loading dashboard:", error);

        }

    };

    if (!dashboard) {

        return (

            <MainLayout>

                <div className="flex items-center justify-center h-96">

                    <h2 className="text-xl font-semibold text-slate-600 dark:text-slate-300">
                        Loading Dashboard...
                    </h2>

                </div>

            </MainLayout>

        );

    }

    return (

        <MainLayout>

            <div className="space-y-8">

                {/* KPI Cards */}

                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">

                    <StatCard
                        title="Projects"
                        value={dashboard.totalProjects}
                        icon="📁"
                        type="blue"
                    />

                    <StatCard
                        title="Services"
                        value={dashboard.totalApiServices}
                        icon="📦"
                        type="yellow"
                    />

                    <StatCard
                        title="Endpoints"
                        value={dashboard.totalEndpoints}
                        icon="🌐"
                        type="green"
                    />

                    <StatCard
                        title="Active Alerts"
                        value={dashboard.activeAlerts}
                        icon="🚨"
                        type="red"
                    />

                </div>

                {/* Health & Performance */}

                <div className="grid grid-cols-1 gap-6">

                    <ChartCard
                        metrics={metrics}
                    />

                </div>

                {/* Alerts & Slow Endpoints */}

                <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">

                    <AlertsCard
                        alerts={dashboard.alerts || []}
                    />

                    <SlowApiTable
                        metrics={metrics}
                    />

                </div>

            </div>

        </MainLayout>

    );

}

export default Dashboard;