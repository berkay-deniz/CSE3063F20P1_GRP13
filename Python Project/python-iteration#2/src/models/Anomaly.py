class Anomaly:
    def __init__(self, name, email):
        self.name = name
        self.email = email

    def to_dict(self):
        return {"student_email": self.email, "student_name": self.name}
