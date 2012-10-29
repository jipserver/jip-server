from setuptools import setup

setup(
        name='Jip-Client',
        version='1.0',
        description='Jip Client library',
        author='Thasso Griebel',
        author_email='thasso.griebel@gmail.com',
        url='',
        long_description='''
        The JIP client library to interact with the JIP server and the client cluster
        ''',
        packages=['jip'],
        entry_points = {
            'console_scripts': [
                'jip = jip:main',
            ],
        }
)
