import pandas as pd


def readStudents(studentList):
    pd.set_option('expand_frame_repr', False)
    pd.set_option("display.max_columns", 999)
    pd.set_option('display.max_rows', 999)

    df = pd.read_excel(studentList, header=12, usecols='C,E,H')

    df = df[(df['Öğrenci No'].notnull()) & (df['Öğrenci No'] != 'Öğrenci No')]

    df["FullName"] = df["Adı"] + " " + df["Soyadı"]

    del df['Adı']
    del df['Soyadı']

    df.to_excel('StudentList.xlsx')



#readStudents("CES3063_Fall2020_rptSinifListesi.XLS")
