from difflib import SequenceMatcher


def similar(a, b):
    return SequenceMatcher(None, a, b).ratio()


class Student:
    attendance = 0

    def __init__(self, id, name, email):
        self.id = id
        self.name = name
        self.email = email

    def match(self, student_name):
        translationTable1 = str.maketrans("ğĞıİöÖüÜşŞçÇ", "gGiIoOuUsScC")
        translationTable2 = str.maketrans("IİÖÜ", "ıiöü")
        numbers = "0123456789"
        temp = student_name
        student_name = ""
        for char in temp:
            if char not in numbers:
                student_name += char
        if (similar(self.name.lower(), student_name.lower()) > 0.95) or \
                (similar(self.name.translate(translationTable1).lower(), student_name.lower()) > 0.95) or \
                (similar(self.name.translate(translationTable2).lower(), student_name.lower()) > 0.95):
            return True
        else:
            full_name = self.name.split()
            last_name = full_name[-1]
            student_full_name = student_name.split()
            student_last_name = student_full_name[-1]
            if (student_last_name.lower() == last_name.lower()) or \
                    (student_last_name.lower() == last_name.translate(translationTable2).lower()) or \
                    (student_last_name.lower() == last_name.translate(translationTable1).lower()):
                for name_element in full_name:
                    if (similar(name_element.lower(), student_full_name[0].lower()) > 0.90) or \
                             (similar(name_element.translate(translationTable1).lower(), student_full_name[0].lower()) > 0.90) or \
                            (similar(name_element.translate(translationTable2).lower(), student_full_name[0].lower()) > 0.90):
                        return True
            return False

    def similarity(self, student_name):
        numbers = "0123456789"
        temp = student_name
        student_name = ""
        for char in temp:
            if char not in numbers:
                student_name += char
        return similar(self.name.lower(), student_name.lower())


