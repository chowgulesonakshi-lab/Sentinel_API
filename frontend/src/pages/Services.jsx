import { useEffect, useState } from "react";
import MainLayout from "../layouts/MainLayout";
import { getServices, updateService, deleteService, getServicesByProject  } from "../services/apiServiceService";
import ServiceModal from "../components/ServiceComponents/ServiceModal";
import { useSearchParams } from "react-router-dom";
import { FolderKanban, Circle } from "lucide-react";

function Services() {
    const [searchParams] = useSearchParams();
    const projectId = searchParams.get("projectId");
    const [services, setServices] = useState([]);
    const [expandedProjects, setExpandedProjects] = useState({});
    const [showModal, setShowModal] = useState(false);
    const [selectedService, setSelectedService] = useState(null);
    useEffect(() => {
        loadServices();
    }, [projectId]);


    const loadServices = async () => {
        try {
            const data = projectId
                        ? await getServicesByProject(projectId)
                        : await getServices();

                    setServices(data);

                } catch (error) {
                    console.error("Error loading services:", error);
                }
    };

    const handleEditService = async (serviceData) => {
        try {
            await updateService(selectedService.id, serviceData);
            setShowModal(false);
            setSelectedService(null);
            loadServices();
        } catch (error) {
            console.error("Error updating service:", error);
        }
    };

    const handleDeleteService = async (id) => {
        const confirmDelete = window.confirm("Are you sure you want to delete this service? All associated endpoints, metrics, and alerts will also be deleted.");
        if (!confirmDelete) return;
        try {
            await deleteService(id);
            loadServices();
        } catch (error) {
            console.error("Error deleting service:", error);
            alert("Failed to delete service: " + (error.response?.data?.message || error.message));
        }
    };

    const groupedServices = services.reduce((groups, service) => {
        const project = service.projectName || "No Project";
        if (!groups[project]) {
            groups[project] = [];
        }
        groups[project].push(service);
        return groups;
    }, {});


    return (
        <MainLayout>
            <div className="space-y-6">
                {/* Header */}
                <div className="flex justify-between items-center">
                    <div>
                        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
                            Services
                        </h1>
                        <p className="mt-2 text-slate-500 dark:text-slate-400">
                            Monitor all registered API services.
                        </p>
                    </div>
                    <button
                        onClick={() => setShowModal(true)}
                        className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded-lg transition"
                    >
                        + Add Service
                    </button>
                </div>

                {/* Service Cards */}
                {services.length === 0 ? (
                    <div className="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl p-6 shadow-sm">
                        <p className="text-slate-600 dark:text-slate-300">
                            No services added yet.
                        </p>
                    </div>
                ) : (
                    <div className="space-y-10">
                        {Object.entries(groupedServices).map(([projectName, projectServices]) => {
                            const expanded = expandedProjects[projectName];
                            const visibleServices = expanded
                                ? projectServices
                                : projectServices.slice(0, 5);
                            return (
                                <div key={projectName}>
                                    <div className="flex items-center justify-between border-b border-slate-200 dark:border-slate-700 pb-3 mb-6">
                                        <div>
                                            <h2 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
                                                <FolderKanban size={22} strokeWidth={1.8} />
                                                {projectName}
                                            </h2>
                                            <p className="text-slate-500 dark:text-slate-400">
                                                {projectServices.length} Services
                                            </p>
                                        </div>
                                    </div>
                                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                                        {visibleServices.map((service) => (
                                            <div
                                                key={service.id}
                                                className="bg-white dark:bg-slate-800 border border-slate-200
                                                dark:border-slate-700 rounded-xl p-6 shadow-sm hover:shadow-lg
                                                transition">
                                                <div className="flex justify-between items-start">
                                                    <div>
                                                        <h2 className="text-xl font-semibold text-slate-900 dark:text-white">
                                                            {service.name}
                                                        </h2>
                                                        <p className="mt-2 text-slate-500 dark:text-slate-400">
                                                            {service.description}
                                                        </p>
                                                    </div>
                                                    <span
                                                        className={`text-xs font-medium px-3 py-1 rounded-full flex items-center gap-1 w-fit ${
                                                            service.status === "ACTIVE"
                                                                ? "bg-emerald-100 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400"
                                                                : "bg-red-100 text-red-600 dark:bg-red-500/10 dark:text-red-400"
                                                        }`}
                                                    >
                                                        <Circle
                                                            size={7}
                                                            fill="currentColor"
                                                            strokeWidth={0}
                                                        />
                                                        {service.status}
                                                    </span>
                                                </div>
                                                <div className="mt-5 space-y-2 text-sm text-slate-600 dark:text-slate-400">
                                                    <p>
                                                        <span className="font-medium">
                                                            Project:
                                                        </span>{" "}
                                                        {service.projectName}
                                                    </p>
                                                    <p>
                                                        <span className="font-medium">
                                                            Status:
                                                        </span>{" "}
                                                        {service.status}
                                                    </p>
                                                </div>
                                                <div className="flex justify-end gap-3 mt-6">
                                                    <button
                                                        onClick={() => {
                                                            setSelectedService(service);
                                                            setShowModal(true);
                                                        }}
                                                        className="
                                                        px-4 py-2 border border-slate-300 dark:border-slate-600
                                                        rounded-lg hover:bg-slate-100 dark:hover:bg-slate-700
                                                        text-slate-900 dark:text-white transition-colors">
                                                        Edit
                                                    </button>

                                                    <button
                                                        onClick={() => handleDeleteService(service.id)}
                                                        className="px-4 py-2 border border-red-500 text-red-500
                                                        rounded-lg hover:bg-red-50 dark:hover:bg-red-900/20
                                                        transition-colors">
                                                        Delete
                                                    </button>
                                                </div>
                                            </div>
                                            ))}
                                            </div>
                                            {projectServices.length > 5 && (
                                                <div className="flex justify-center mt-6">
                                                    <button
                                                        onClick={() =>
                                                            setExpandedProjects((prev) => ({
                                                                ...prev,
                                                                [projectName]: !prev[projectName]
                                                            }))
                                                        }
                                                        className="text-blue-600 hover:text-blue-700 font-semibold"
                                                    >
                                                        {expanded
                                                            ? "Show Less"
                                                            : `View All (${projectServices.length})`}
                                                    </button>
                                                </div>
                                            )}
                                            </div>
                                            );
                                            })}
                                            </div>
                                            )}
                <ServiceModal
                    isOpen={showModal}
                    onClose={() => {
                        setShowModal(false);
                        setSelectedService(null);
                    }}
                    refreshServices={loadServices}
                    selectedService={selectedService}
                    onSave={handleEditService}
                    projectId={projectId}
                />
            </div>
        </MainLayout>
    );
}

export default Services;