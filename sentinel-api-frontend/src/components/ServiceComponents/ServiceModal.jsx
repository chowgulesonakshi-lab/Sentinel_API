import { useState } from "react";
import { createService } from "../../services/apiServiceService";

function ServiceModal({
    isOpen,
    onClose,
    refreshServices,
    projectId
}) {

    const [service, setService] = useState({
        name: "",
        description: "",
        projectId: projectId,
        status: "ACTIVE"
    });


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
                projectId: Number(projectId)
            };

            console.log("Creating service:", serviceData);

            await createService(serviceData);


            setService({
                name: "",
                description: "",
                projectId: projectId,
                status: "ACTIVE"
            });


            onClose();
            refreshServices();


        } catch (error) {

            console.error("Error creating service:", error);

        }
    };


    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[100]">

            <div className="bg-white dark:bg-slate-900 rounded-2xl p-6 w-full max-w-md shadow-xl border border-slate-200 dark:border-slate-700">

                <h2 className="text-xl font-bold text-slate-900 dark:text-white mb-5">
                    Add Service
                </h2>


                <form onSubmit={handleSubmit} className="space-y-4">


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
                        <option value="ACTIVE">
                            Active
                        </option>

                        <option value="INACTIVE">
                            Inactive
                        </option>

                        <option value="MAINTENANCE">
                            Maintenance
                        </option>

                    </select>


                    <div className="flex justify-end gap-3">

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
                            Save
                        </button>

                    </div>

                </form>

            </div>

        </div>
    );
}

export default ServiceModal;