import { useState, useEffect } from "react";

function ProjectModal({ isOpen, onClose, onSave, project }) {
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");

    useEffect(() => {
        if (project) {
            setName(project.name);
            setDescription(project.description);
        } else {
            setName("");
            setDescription("");
        }
    }, [project]);

    if (!isOpen) return null;

    const handleSubmit = (e) => {
        e.preventDefault();

        onSave({
            name,
            description,
        });
    };

    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50">

            <div className="bg-white dark:bg-slate-800 rounded-xl shadow-xl w-full max-w-md
                p-6 border border-slate-200 dark:border-slate-700 transition-colors">

                <h2 className="text-2xl font-bold mb-6 text-slate-900 dark:text-white">
                    {project ? "Edit Project" : "New Project"}
                </h2>

                <form onSubmit={handleSubmit} className="space-y-5">

                    <div>
                        <label className="block text-sm font-medium mb-2 text-slate-700 dark:text-slate-300">
                            Project Name
                        </label>

                        <input
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            className="w-full border border-slate-300 dark:border-slate-600 rounded-lg px-4 py-2
                                bg-white dark:bg-slate-700 text-slate-900 dark:text-white placeholder:text-slate-400
                                focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium mb-2 text-slate-700 dark:text-slate-300">
                            Description
                        </label>

                        <textarea
                            rows="3"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            className="w-full border border-slate-300 dark:border-slate-600 rounded-lg px-4 py-2
                                bg-white dark:bg-slate-700 text-slate-900 dark:text-white placeholder:text-slate-400
                                focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                        />
                    </div>

                    <div className="flex justify-end gap-3 pt-4">

                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 border border-slate-300 dark:border-slate-600 rounded-lg
                                text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
                        >
                            Cancel
                        </button>

                        <button
                            type="submit"
                            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
                        >
                            {project ? "Update" : "Create"}
                        </button>

                    </div>

                </form>

            </div>

        </div>
    );
}

export default ProjectModal;