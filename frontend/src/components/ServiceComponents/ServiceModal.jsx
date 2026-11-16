import { useState, useEffect } from "react";
import { createService } from "../../services/apiServiceService";
import { getProjects } from "../../services/projectService";

function ServiceModal({
    isOpen,
    onClose,
    refreshServices,
    projectId,
    selectedService,
    onSave
}) {
    const [projects, setProjects] = useState([]);
    const [service, setService] = useState({
        name: "",
        description: "",
        projectId: projectId || "",
        status: "ACTIVE"
    });

    useEffect(() => {
        if (!isOpen) return;

        if (!projectId) {
            loadProjects();
        }

        if (selectedService) {
            setService({
                name: selectedService.name || "",
                description: selectedService.description || "",
                projectId: selectedService.projectId || projectId || "",
                status: selectedService.status || "ACTIVE"
            });
        } else {
            setService({
                name: "",
                description: "",
                projectId: projectId || "",
                status: "ACTIVE"
            });
        }
    }, [isOpen, selectedService, projectId]);

    const loadProjects = async () => {
        try {
            const data = await getProjects();
            setProjects(data);
        } catch (error) {
            console.error("Error fetching projects for ServiceModal:", error);
        }
    };

    if (!isOpen) return null;

    const handleChange = (e) => {
        setService({
            ...service,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const serviceData = {
                ...service,
                projectId: Number(service.projectId || projectId)
            };

            if (selectedService && onSave) {
                await onSave(serviceData);
            } else {
                await createService(serviceData);
            }

            onClose();
            refreshServices();
        } catch (error) {
            console.error("Error saving service:", error);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[100]">
            <div className="bg-white dark:bg-slate-900 rounded-2xl p-6 w-full max-w-md shadow-xl border border-slate-200 dark:border-slate-700">
                <h2 className="text-xl font-bold text-slate-900 dark:text-white mb-5">
                    {selectedService ? "Edit Service" : "Add Service"}
                </h2>

                <form onSubmit={handleSubmit} className="space-y-4">
                    {!projectId && (
                        <select
                            name="projectId"
                            value={service.projectId}
                            onChange={handleChange}
                            className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 dark:bg-slate-800 dark:text-white"
                            required
                        >
                            <option value="">Select Target Project</option>
                            {projects.map((proj) => (
                                <option key={proj.id} value={proj.id}>
                                    {proj.name}
                                </option>
                            ))}
                        </select>
                    )}

                    <input
                        type="text"
                        name="name"
                        placeholder="Service Name"
                        value={service.name}
                        onChange={handleChange}
                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 dark:bg-slate-800 dark:text-white"
                        required
                    />

                    <textarea
                        name="description"
                        placeholder="Description"
                        value={service.description}
                        onChange={handleChange}
                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 dark:bg-slate-800 dark:text-white"
                        required
                    />

                    <select
                        name="status"
                        value={service.status}
                        onChange={handleChange}
                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 dark:bg-slate-800 dark:text-white"
                    >
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                        <option value="MAINTENANCE">Maintenance</option>
                    </select>

                    <div className="flex justify-end gap-3 pt-2">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800"
                        >
                            Cancel
                        </button>

                        <button
                            type="submit"
                            className="px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700"
                        >
                            {selectedService ? "Update" : "Save"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default ServiceModal;