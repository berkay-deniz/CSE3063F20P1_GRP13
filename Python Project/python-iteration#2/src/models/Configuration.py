import json


class Configuration:
    def __init__(self):
        config_file = open('../../config.json', 'r')
        data = json.load(config_file)
        self.log_file_path = data["log_file_path"]
        self.student_list_file_path = data["student_list_file_path"]
        self.answer_keys_dir_path = data["answer_keys_dir_path"]
        self.poll_reports_dir_path = data["poll_reports_dir_path"]
        self.exact_match_threshold = data["exact_match_threshold"]
        self.partial_match_threshold = data["partial_match_threshold"]
        self.max_similarity_threshold = data["max_similarity_threshold"]
        self.organized_student_list_file_path = data["organized_student_list_file_path"]
        self.attendance_report_file_path = data["attendance_report_file_path"]
        self.plots_dir_path = data["plots_dir_path"]
        self.global_analytics_file_path = data["global_analytics_file_path"]
        self.poll_results_dir_path = data["poll_results_dir_path"]
        self.student_poll_results_dir_path = data["student_poll_results_dir_path"]
        self.absences_and_anomalies_dir_path = data["absences_and_anomalies_dir_path"]
        config_file.close()
