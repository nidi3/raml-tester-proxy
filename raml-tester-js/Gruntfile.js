/*global module*/

var version = "0.8.6";

module.exports = function (grunt) {
    grunt.initConfig({
        clean: {
            clean: ['bin', 'lib']
        },
        copy: {
            dist: {
                files: [
                    {
                        src: process.env['HOME'] + '/.m2/repository/guru/nidi/raml/raml-tester-standalone/' + version + '/raml-tester-standalone-' + version + '.jar',
                        dest: 'bin/raml-tester-standalone.jar'
                    }, {
                        cwd: 'src/',
                        expand: true,
                        src: ['raml-tester.js', 'raml-tester-browser.js'],
                        dest: 'lib'
                    }, {
                        cwd: 'src/',
                        expand: true,
                        src: 'raml-tester-cli',
                        dest: 'bin'
                    }]
            }
        },
        mochacli: {
            all: ['test/*.js']
        }
    });

    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-mocha-cli');

    grunt.registerTask('default', ['copy']);
    grunt.registerTask('test', ['mochacli']);
};
