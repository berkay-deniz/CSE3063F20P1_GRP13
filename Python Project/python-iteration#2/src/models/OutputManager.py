import logging
import os
from datetime import datetime

import matplotlib.pyplot as plotter
import pandas as pd


class OutputManager:
    def __init__(self, configuration):
        self.configuration = configuration

    def print_poll_results(self, meetings, students):
        poll_dfs = {}
        for meeting in meetings:
            for poll in meeting.polls:
                poll_result_df = pd.read_excel(self.configuration.organized_student_list_file_path, usecols='B, C')

                question_no = 1
                # Insert one column for each question in the poll
                for question in poll.answer_key.questions:
                    poll_result_df.insert(question_no + 1, question.text, 0)
                    question_no += 1

                poll_result_df.insert(question_no + 1, "Questions", question_no - 1)
                poll_result_df.insert(question_no + 2, "Correct Answers", 0)
                poll_result_df.insert(question_no + 3, "Wrong Answers", 0)
                poll_result_df.insert(question_no + 4, "Empty Answers", 0)
                poll_result_df.insert(question_no + 5, "Success", 0.0)
                poll_result_df.insert(question_no + 6, "Success (%)", 0)

                answers_of_questions = {}
                poll.print_student_results(answers_of_questions, poll_result_df, students, self.configuration)
                self.print_pie_charts(answers_of_questions, poll)

                poll_dfs[poll] = poll_result_df

                if not os.path.exists(self.configuration.poll_results_dir_path):
                    os.makedirs(self.configuration.poll_results_dir_path)
                poll_result_df.to_excel(self.configuration.poll_results_dir_path + "/"
                                        + "Poll_" + poll.poll_id + "_" + poll.name.replace(" ", "_") + "_"
                                        + poll.date.replace(" ", "_").replace("-", "_").replace(":", "_") + ".xlsx")
                logging.info("Results of the poll named '" + poll.name + "' printed to an excel file successfully.")

            self.print_global_results(poll_dfs, meetings)

    def print_global_results(self, poll_dfs, meetings):
        all_polls = []
        for meeting in meetings:
            all_polls += meeting.polls

        all_polls.sort(key=lambda current_poll: datetime.strptime(current_poll.date, "%Y-%m-%d %H:%M:%S"))

        student_result_df = pd.read_excel(self.configuration.organized_student_list_file_path)
        column = len(student_result_df.columns)

        total_num_of_questions = 0
        for poll in all_polls:
            if poll not in poll_dfs.keys():
                continue

            total_num_of_questions += len(poll.answer_key.questions)

            name = poll.name
            occurrence = 2
            while name + " Date" in student_result_df.columns:
                name = poll.name + " (" + str(occurrence) + ")"
                occurrence += 1

            column_name = "Poll_" + poll.poll_id + "_" + poll.name.replace(" ", "_") + "_" \
                          + poll.date.replace(" ", "_").replace("-", "_").replace(":", "_")
            student_result_df.insert(column, column_name, poll.date)
            column += 1

            df = poll_dfs[poll]
            student_result_df[column_name] = df["Correct Answers"]

        student_result_df.insert(column, "Total Accuracy (%)", 0)
        student_result_df["Total Accuracy (%)"] = \
            100 * student_result_df.iloc[:, 3 - len(student_result_df.columns):].sum(axis=1) / total_num_of_questions

        student_result_df.to_excel(self.configuration.global_analytics_file_path, index=False)
        logging.info("Results of the students for each poll printed to an excel file successfully.")

    def print_pie_charts(self, answers_of_questions, poll):
        question_list = []

        num_of_questions = len(poll.answer_key.questions)
        fig, axs = plotter.subplots(num_of_questions, 2, figsize=(30, num_of_questions * 15))
        plotter.subplots_adjust(wspace=1, hspace=1)
        i = 0
        for question, answer_occurrences in answers_of_questions.items():
            pie_location = axs[0] if num_of_questions == 1 else axs[i, 0]
            bar_location = axs[1] if num_of_questions == 1 else axs[i, 1]
            current_label_unicode = ord("A")
            ans_labels = []
            ans_list = []
            occurrence_list = []
            question_list.append(question)
            for answer, occurrence in answer_occurrences.items():
                ans_list.append(answer)
                ans_labels.append(chr(current_label_unicode))
                current_label_unicode += 1
                occurrence_list.append(occurrence)

            explode = []
            pie_chart_colors_lookup = ['#3352FF', '#FF4C33', '#FFFF33', '#33FFF3', '#FF339F', '#FF9933']
            pie_chart_colors = []
            x = 0
            for answer in ans_list:
                if self.is_answer_true(question, answer, poll):
                    pie_chart_colors.append("#009900")
                    explode.append(0.1)
                else:
                    pie_chart_colors.append(pie_chart_colors_lookup[x])
                    explode.append(0)
                    x += 1

            pie_location.pie(occurrence_list, explode=explode, labels=ans_labels,
                             autopct=self.make_autopct(occurrence_list),
                             shadow=True, colors=pie_chart_colors)
            question_string = "Question: " + question if len(question) < 150 else question[:int(
                len(question) / 2)] + "\n" + question[int(len(question) / 2):]
            pie_location.title.set_text(question_string)
            bar_location.bar(ans_labels, occurrence_list, width=0.8,
                             color=["#009900" if self.is_answer_true(question, answer, poll) else "#b20000" for
                                    answer
                                    in ans_list], bottom=None, align='center', data=occurrence_list)
            bar_location.title.set_text(question_string)
            label_str = ""
            for j in range(0, len(ans_list)):
                label_str = label_str + "\n" + ans_labels[j] + ": " + ans_list[j]
            pie_location.set_xlabel(label_str)
            bar_location.set_xlabel(label_str)
            i += 1

        folder_path = self.configuration.plots_dir_path
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
        plotter.savefig(folder_path + "/" + poll.name + ".pdf")
        logging.info("Plots of the poll named '" + poll.name + "' printed to a pdf file successfully.")

    def make_autopct(self, values):
        def my_autopct(pct):
            total = sum(values)
            val = int(round(pct * total / 100.0))
            return '{v:d}'.format(v=val)

        return my_autopct

    def is_answer_true(self, question, answer, poll):
        for x in range(0, len(poll.answer_key.questions), 1):
            if question == poll.answer_key.questions[x].text:
                for correct_answer in poll.answer_key.questions[x].correct_answers:
                    if answer == correct_answer:
                        return True
                return False

    def print_absences_and_anomalies(self, meetings, students):
        for meeting in meetings:
            for poll in meeting.polls:
                poll.print_absences_and_anomalies(students, self.configuration)

    def print_attendance_report(self, total_days, students):
        attendance_df = pd.read_excel(self.configuration.organized_student_list_file_path, usecols='B,C')
        attendance_df.insert(2, "Total Meeting Days", total_days)
        attendance_df.insert(3, "Attendance", 0)
        attendance_df.insert(4, "Attendance Rate", 0.0)
        attendance_df.insert(5, "Attendance Percentage", 0)

        for i in range(0, len(students.values())):
            current_student = students[attendance_df.at[i, 'Öğrenci No']]
            attendance_df.at[i, 'Attendance'] = current_student.attendance
            attendance_df.at[i, 'Attendance Rate'] = current_student.attendance / total_days
            attendance_df.at[i, 'Attendance Percentage'] = (current_student.attendance / total_days) * 100

        attendance_df.to_excel(self.configuration.attendance_report_file_path)
        logging.info("Attendances of the students printed to an excel file successfully.")
