import re


class AnswerKey:
    def __init__(self, file_path):
        # TODO: integrate to the changed format
        answer_key_file = open(file_path, "r")
        answer_key_string = answer_key_file.read()
        regex = re.compile(r'\"(.+?)\"', flags=re.DOTALL)
        split_string = regex.findall(answer_key_string)

        poll_name = split_string[0]
        q_and_a = []

        for x in range(1, len(split_string) - 1, 2):
            q_and_a.append(split_string[x])
            q_and_a.append(split_string[x + 1])
        self.name = poll_name
        self.answer_key = q_and_a
        self.poll_name = poll_name
        self.q_and_a = q_and_a
